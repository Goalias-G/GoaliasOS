package com.goalias.common.oss.core;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.goalias.common.oss.config.properties.MinioProperties;
import com.goalias.common.oss.constant.OssConstants;
import com.goalias.common.oss.domain.dto.MultipartUploadInitDTO;
import com.goalias.common.oss.domain.dto.MultipartUploadMergeDTO;
import com.goalias.common.oss.domain.vo.ChunkUploadVO;
import com.goalias.common.oss.domain.vo.MultipartUploadInitVO;
import com.goalias.common.oss.domain.vo.MultipartUploadResultVO;
import com.goalias.common.redis.service.RedisService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 文件服务实现
 * 支持普通上传和分片上传（大文件）
 * 
 * 分片上传采用 composeObject 方案：
 * 1. 每个分片作为独立临时对象上传
 * 2. 所有分片上传完成后，使用 composeObject 合并
 * 3. 合并后删除临时分片对象
 *
 * @author Goalias
 */
@Component
@RequiredArgsConstructor
public class MinioService implements IFileService {

    private final MinioProperties properties;
    private final MinioClient client;
    private final RedisService redisService;

    private static final Logger logger = LoggerFactory.getLogger(MinioService.class);

    // ==================== 普通上传方法 ====================

    /**
     * 上传文件（流式）
     *
     * @param bucketName  存储桶
     * @param objectName  文件对象名
     * @param inputStream 文件输入流
     * @return 文件名
     */
    @Override
    public String uploadFile(String bucketName, String objectName, InputStream inputStream) {
        try (inputStream) {
            ObjectWriteResponse owr = client.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, inputStream.available(), -1)
                    .build());
            return owr.object();
        } catch (Exception e) {
            logger.error("上传文件失败: {}", ExceptionUtil.stacktraceToString(e));
            return null;
        }
    }

    /**
     * 上传本地文件
     *
     * @param bucketName 存储桶
     * @param objectName 文件对象名
     * @param filePath   本地文件路径
     * @return 文件名
     */
    @Override
    public String uploadLocalFile(String bucketName, String objectName, String filePath) {
        try {
            ObjectWriteResponse owr = client.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .filename(filePath)
                            .build());
            return owr.object();
        } catch (Exception e) {
            logger.error("上传本地文件失败: {}", ExceptionUtil.stacktraceToString(e));
            return null;
        }
    }

    /**
     * 上传 MultipartFile 文件
     *
     * @param bucketName 存储桶
     * @param objectName 文件对象名
     * @param file       MultipartFile 文件
     * @return 文件访问 URL
     */
    @Override
    public String uploadFile(String bucketName, String objectName, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return getUrl(bucketName, objectName);
        } catch (Exception e) {
            logger.error("上传 MultipartFile 失败: {}", ExceptionUtil.stacktraceToString(e));
            return null;
        }
    }

    // ==================== 分片上传方法 ====================

    /**
     * 初始化分片上传
     * 前端在开始分片上传前调用，获取 uploadId 和预签名 URL 列表
     *
     * @param dto 初始化请求参数
     * @return 分片上传初始化信息
     */
    @Override
    public MultipartUploadInitVO initMultipartUpload(MultipartUploadInitDTO dto) {
        try {
            // 1. 检查存储桶是否存在，不存在则创建
            ensureBucketExists(dto.getBucketName());

            // 2. 秒传检查：如果提供了 MD5，检查文件是否已存在
            if (StrUtil.isNotBlank(dto.getFileMd5())) {
                String existingUrl = checkInstantUpload(dto.getBucketName(), dto.getFileMd5());
                if (existingUrl != null) {
                    logger.info("秒传成功，文件已存在: {}", existingUrl);
                    return MultipartUploadInitVO.builder()
                            .instantUpload(true)
                            .fileUrl(existingUrl)
                            .objectName(dto.getObjectName())
                            .bucketName(dto.getBucketName())
                            .build();
                }
            }

            // 3. 计算分片信息
            long chunkSize = Math.max(dto.getChunkSize(), OssConstants.Multipart.MIN_CHUNK_SIZE);
            int totalChunks = (int) Math.ceil((double) dto.getFileSize() / chunkSize);

            // 校验分片数量
            if (totalChunks > OssConstants.Multipart.MAX_CHUNK_COUNT) {
                throw new IllegalArgumentException("分片数量超过限制，请增大分片大小");
            }

            // 4. 生成唯一的 uploadId
            String uploadId = IdUtil.fastSimpleUUID();

            // 5. 生成各分片的预签名上传 URL
            List<String> uploadUrls = generateChunkPresignedUrls(dto.getBucketName(), dto.getObjectName(), uploadId, totalChunks);

            // 6. 缓存上传信息（用于断点续传和合并）
            cacheUploadInfo(uploadId, dto, totalChunks);

            logger.info("分片上传初始化成功，uploadId: {}, 总分片数: {}", uploadId, totalChunks);

            return MultipartUploadInitVO.builder()
                    .uploadId(uploadId)
                    .bucketName(dto.getBucketName())
                    .objectName(dto.getObjectName())
                    .totalChunks(totalChunks)
                    .chunkSize(chunkSize)
                    .instantUpload(false)
                    .uploadUrls(uploadUrls)
                    .uploadedChunks(new ArrayList<>())
                    .build();

        } catch (Exception e) {
            logger.error("初始化分片上传失败: {}", ExceptionUtil.stacktraceToString(e));
            throw new RuntimeException("初始化分片上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传单个分片（通过后端代理）
     * 分片会作为独立的临时对象存储，合并时再组合
     *
     * @param bucketName  存储桶
     * @param objectName  文件对象名
     * @param uploadId    分片上传 ID
     * @param chunkNumber 分片编号（从 1 开始）
     * @param file        分片文件
     * @return 分片上传结果
     */
    @Override
    public ChunkUploadVO uploadChunk(String bucketName, String objectName, String uploadId,
                                     Integer chunkNumber, MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            // 生成分片临时对象名
            String chunkObjectName = getChunkObjectName(objectName, uploadId, chunkNumber);

            // 上传分片到 MinIO
            ObjectWriteResponse response = client.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(chunkObjectName)
                    .stream(inputStream, file.getSize(), -1)
                    .build());

            // 记录已上传的分片
            recordUploadedChunk(uploadId, chunkNumber, response.etag());

            logger.info("分片上传成功，uploadId: {}, 分片: {}", uploadId, chunkNumber);

            return ChunkUploadVO.builder()
                    .chunkNumber(chunkNumber)
                    .etag(response.etag())
                    .success(true)
                    .build();

        } catch (Exception e) {
            logger.error("分片上传失败，uploadId: {}, 分片: {}, 错误: {}",
                    uploadId, chunkNumber, ExceptionUtil.stacktraceToString(e));
            return ChunkUploadVO.builder()
                    .chunkNumber(chunkNumber)
                    .success(false)
                    .errorMsg(e.getMessage())
                    .build();
        }
    }

    /**
     * 合并分片完成上传
     * 使用 composeObject 将所有分片合并为最终文件
     *
     * @param dto 合并请求参数
     * @return 上传结果
     */
    @Override
    public MultipartUploadResultVO completeMultipartUpload(MultipartUploadMergeDTO dto) {
        try {
            // 1. 获取上传信息
            Map<String, Object> uploadInfo = getUploadInfo(dto.getUploadId());
            if (uploadInfo == null) {
                throw new RuntimeException("上传任务不存在或已过期");
            }

            int totalChunks = Integer.parseInt(uploadInfo.get("totalChunks").toString());

            // 2. 构建分片源列表
            List<ComposeSource> sources = new ArrayList<>();
            for (int i = 1; i <= totalChunks; i++) {
                String chunkObjectName = getChunkObjectName(dto.getObjectName(), dto.getUploadId(), i);
                sources.add(ComposeSource.builder()
                        .bucket(dto.getBucketName())
                        .object(chunkObjectName)
                        .build());
            }

            // 3. 合并分片
            ObjectWriteResponse response = client.composeObject(ComposeObjectArgs.builder()
                    .bucket(dto.getBucketName())
                    .object(dto.getObjectName())
                    .sources(sources)
                    .build());

            // 4. 获取合并后的文件信息
            StatObjectResponse stat = client.statObject(StatObjectArgs.builder()
                    .bucket(dto.getBucketName())
                    .object(dto.getObjectName())
                    .build());

            // 5. 删除临时分片对象
            deleteChunkObjects(dto.getBucketName(), dto.getObjectName(), dto.getUploadId(), totalChunks);

            // 6. 缓存文件 MD5 映射（用于秒传）
            String fileMd5 = uploadInfo.get("fileMd5") != null ? uploadInfo.get("fileMd5").toString() : null;
            if (StrUtil.isNotBlank(fileMd5)) {
                cacheFileMd5Mapping(dto.getBucketName(), fileMd5, dto.getObjectName());
            }

            // 7. 清理缓存
            cleanupUploadCache(dto.getUploadId());

            String fileUrl = getUrl(dto.getBucketName(), dto.getObjectName());
            logger.info("分片上传合并成功，文件: {}", fileUrl);

            return MultipartUploadResultVO.builder()
                    .objectName(dto.getObjectName())
                    .fileUrl(fileUrl)
                    .fileSize(stat.size())
                    .etag(response.etag())
                    .build();

        } catch (Exception e) {
            logger.error("合并分片失败: {}", ExceptionUtil.stacktraceToString(e));
            throw new RuntimeException("合并分片失败: " + e.getMessage());
        }
    }

    /**
     * 取消分片上传
     * 删除所有已上传的临时分片对象
     *
     * @param bucketName 存储桶
     * @param objectName 文件对象名
     * @param uploadId   分片上传 ID
     */
    @Override
    public void abortMultipartUpload(String bucketName, String objectName, String uploadId) {
        try {
            // 获取上传信息
            Map<String, Object> uploadInfo = getUploadInfo(uploadId);
            if (uploadInfo != null) {
                int totalChunks = Integer.parseInt(uploadInfo.get("totalChunks").toString());
                // 删除临时分片对象
                deleteChunkObjects(bucketName, objectName, uploadId, totalChunks);
            }

            // 清理缓存
            cleanupUploadCache(uploadId);

            logger.info("取消分片上传成功，uploadId: {}", uploadId);
        } catch (Exception e) {
            logger.error("取消分片上传失败: {}", ExceptionUtil.stacktraceToString(e));
        }
    }

    /**
     * 获取已上传的分片列表（用于断点续传）
     *
     * @param uploadId 分片上传 ID
     * @return 已上传的分片编号列表
     */
    @Override
    public List<Integer> getUploadedChunks(String uploadId) {
        String cacheKey = OssConstants.CacheKey.UPLOADED_CHUNKS_PREFIX + uploadId;
        Set<Object> chunks = redisService.sMembers(cacheKey);
        if (chunks == null || chunks.isEmpty()) {
            return new ArrayList<>();
        }
        return chunks.stream()
                .map(obj -> {
                    String str = obj.toString();
                    int colonIndex = str.indexOf(':');
                    return colonIndex > 0 ? Integer.parseInt(str.substring(0, colonIndex)) : Integer.parseInt(str);
                })
                .sorted()
                .toList();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成分片临时对象名
     * 格式：.chunks/{uploadId}/{objectName}.part{chunkNumber}
     */
    private String getChunkObjectName(String objectName, String uploadId, int chunkNumber) {
        return String.format(".chunks/%s/%s.part%d", uploadId, objectName, chunkNumber);
    }

    /**
     * 生成分片预签名上传 URL 列表
     */
    private List<String> generateChunkPresignedUrls(String bucketName, String objectName,
                                                     String uploadId, int totalChunks) throws Exception {
        List<String> urls = new ArrayList<>(totalChunks);

        for (int partNumber = 1; partNumber <= totalChunks; partNumber++) {
            String chunkObjectName = getChunkObjectName(objectName, uploadId, partNumber);

            String url = client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucketName)
                    .object(chunkObjectName)
                    .expiry(OssConstants.Multipart.PRESIGNED_URL_EXPIRY, TimeUnit.SECONDS)
                    .build());
            urls.add(url);
        }

        return urls;
    }

    /**
     * 删除临时分片对象
     */
    private void deleteChunkObjects(String bucketName, String objectName, String uploadId, int totalChunks) {
        try {
            List<DeleteObject> deleteObjects = new ArrayList<>();
            for (int i = 1; i <= totalChunks; i++) {
                String chunkObjectName = getChunkObjectName(objectName, uploadId, i);
                deleteObjects.add(new DeleteObject(chunkObjectName));
            }

            Iterable<Result<DeleteError>> results = client.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(bucketName)
                    .objects(deleteObjects)
                    .build());

            // 消费结果以确保删除执行
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                logger.warn("删除分片对象失败: {}, {}", error.objectName(), error.message());
            }
        } catch (Exception e) {
            logger.error("删除临时分片对象失败: {}", ExceptionUtil.stacktraceToString(e));
        }
    }

    /**
     * 确保存储桶存在
     */
    private void ensureBucketExists(String bucketName) throws Exception {
        if (!isBucketExist(bucketName)) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            logger.info("创建存储桶: {}", bucketName);
        }
    }

    /**
     * 检查秒传（文件是否已存在）
     */
    private String checkInstantUpload(String bucketName, String fileMd5) {
        String cacheKey = OssConstants.CacheKey.FILE_MD5_PREFIX + bucketName + ":" + fileMd5;
        Object obj = redisService.get(cacheKey);
        String objectName = obj != null ? obj.toString() : null;
        if (StrUtil.isNotBlank(objectName) && isObjectExist(bucketName, objectName)) {
            return getUrl(bucketName, objectName);
        }
        return null;
    }

    /**
     * 缓存上传信息
     */
    private void cacheUploadInfo(String uploadId, MultipartUploadInitDTO dto, int totalChunks) {
        String cacheKey = OssConstants.CacheKey.MULTIPART_UPLOAD_PREFIX + uploadId;
        Map<String, Object> uploadInfo = new HashMap<>();
        uploadInfo.put("bucketName", dto.getBucketName());
        uploadInfo.put("objectName", dto.getObjectName());
        uploadInfo.put("fileSize", dto.getFileSize());
        uploadInfo.put("fileMd5", dto.getFileMd5());
        uploadInfo.put("contentType", dto.getContentType());
        uploadInfo.put("totalChunks", totalChunks);
        uploadInfo.put("createTime", System.currentTimeMillis());

        redisService.hmSet(cacheKey, uploadInfo);
        redisService.expire(cacheKey, OssConstants.CacheExpire.MULTIPART_UPLOAD_EXPIRE);
    }

    /**
     * 获取上传信息
     */
    private Map<String, Object> getUploadInfo(String uploadId) {
        String cacheKey = OssConstants.CacheKey.MULTIPART_UPLOAD_PREFIX + uploadId;
        return redisService.hmGet(cacheKey);
    }

    /**
     * 记录已上传的分片
     */
    private void recordUploadedChunk(String uploadId, Integer chunkNumber, String etag) {
        String cacheKey = OssConstants.CacheKey.UPLOADED_CHUNKS_PREFIX + uploadId;
        redisService.sAdd(cacheKey, chunkNumber + ":" + etag);
        redisService.expire(cacheKey, OssConstants.CacheExpire.MULTIPART_UPLOAD_EXPIRE);
    }

    /**
     * 缓存文件 MD5 映射（用于秒传）
     */
    private void cacheFileMd5Mapping(String bucketName, String fileMd5, String objectName) {
        String cacheKey = OssConstants.CacheKey.FILE_MD5_PREFIX + bucketName + ":" + fileMd5;
        redisService.set(cacheKey, objectName, OssConstants.CacheExpire.FILE_MD5_EXPIRE);
    }

    /**
     * 清理上传缓存
     */
    private void cleanupUploadCache(String uploadId) {
        redisService.del(OssConstants.CacheKey.MULTIPART_UPLOAD_PREFIX + uploadId);
        redisService.del(OssConstants.CacheKey.UPLOADED_CHUNKS_PREFIX + uploadId);
    }

    /**
     * 判断对象是否存在
     */
    @Override
    public boolean isObjectExist(String bucketName, String objectName) {
        try {
            client.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            logger.info("MinIO对象不存在: {}/{}", bucketName, objectName);
            return false;
        }
    }

    /**
     * 删除文件
     */
    @Override
    public void delFile(String bucketName, List<String> objectNames) {
        List<DeleteObject> deleteObjects = new ArrayList<>();
        objectNames.forEach(objectName -> deleteObjects.add(new DeleteObject(objectName)));
        try {
            if (!isBucketExist(bucketName)) {
                return;
            }
            Iterable<Result<DeleteError>> results = client.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(bucketName)
                    .objects(deleteObjects)
                    .build());
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                logger.error("MinIO 删除错误 {}；{}", error.objectName(), error.message());
            }
        } catch (Exception e) {
            logger.error("删除文件失败: {}", ExceptionUtil.stacktraceToString(e));
        }
    }

    /**
     * 获取文件访问路径
     */
    @Override
    public String getUrl(String bucketName, String objectName) {
        return properties.getMediaUrl() + bucketName + "/" + objectName;
    }

    /**
     * 判断存储桶是否存在
     */
    @Override
    public boolean isBucketExist(String bucketName) {
        try {
            return client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            logger.error("检查存储桶失败: {}", ExceptionUtil.stacktraceToString(e));
            return false;
        }
    }

    /**
     * 获取文件扩展名
     */
    public static String getExtension(String fileName) {
        int i = fileName.lastIndexOf(".");
        if (i < 0) return null;
        return fileName.substring(i + 1);
    }

    /**
     * 获取文件扩展名
     */
    public static String getExtension(File file) {
        if (file == null || file.isDirectory()) return null;
        return getExtension(file.getName());
    }

    /**
     * 判断文件是否是图片
     */
    public static boolean isImage(File imgFile) {
        try {
            BufferedImage image = ImageIO.read(imgFile);
            return image != null;
        } catch (IOException e) {
            logger.error("判断图片失败: {}", ExceptionUtil.stacktraceToString(e));
            return false;
        }
    }

    /**
     * 获取时间层次的文件夹路径
     *
     * @return 如：/2026/01/07/
     */
    public static String getTimeFilePath() {
        return new SimpleDateFormat("yyyy/MM/dd").format(new Date()) + "/";
    }

}
