package com.goalias.web.controller;

import com.goalias.common.core.domain.R;
import com.goalias.common.oss.core.MinioService;
import com.goalias.common.oss.domain.dto.MultipartUploadInitDTO;
import com.goalias.common.oss.domain.dto.MultipartUploadMergeDTO;
import com.goalias.common.oss.domain.vo.ChunkUploadVO;
import com.goalias.common.oss.domain.vo.MultipartUploadInitVO;
import com.goalias.common.oss.domain.vo.MultipartUploadResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传控制器
 * 提供普通上传和分片上传接口
 *
 * @author Goalias
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Validated
public class FileController {

    private final MinioService minioService;

    // ==================== 普通上传接口 ====================

    /**
     * 普通文件上传（适用于小文件）
     *
     * @param bucketName 存储桶名称
     * @param file       上传的文件
     * @return 文件访问 URL
     */
    @PostMapping("/upload")
    public R<String> upload(
            @RequestParam(defaultValue = "goalias") String bucketName,
            @RequestParam MultipartFile file) {

        // 生成文件存储路径：/年/月/日/文件名
        //TODO business/userId/timePath/fileName
        String objectName = MinioService.getTimeFilePath() + System.currentTimeMillis()
                + "_" + file.getOriginalFilename();

        String fileUrl = minioService.uploadFile(bucketName, objectName, file);
        if (fileUrl == null) {
            return R.fail("文件上传失败");
        }
        return R.ok("上传成功", fileUrl);
    }

    // ==================== 分片上传接口 ====================

    /**
     * 初始化分片上传
     * 前端在开始分片上传前调用此接口，获取 uploadId 和预签名 URL 列表
     *
     * @param dto 初始化请求参数
     * @return 分片上传初始化信息（包含 uploadId、预签名 URL 列表等）
     */
    @PostMapping("/multipart/init")
    public R<MultipartUploadInitVO> initMultipartUpload(@Valid @RequestBody MultipartUploadInitDTO dto) {
        try {
            MultipartUploadInitVO result = minioService.initMultipartUpload(dto);
            return R.ok("初始化成功", result);
        } catch (Exception e) {
            return R.fail("初始化分片上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传单个分片（通过后端代理）
     * 适用于前端无法直接访问 MinIO 的场景
     *
     * @param bucketName  存储桶名称
     * @param objectName  文件对象名
     * @param uploadId    分片上传 ID
     * @param chunkNumber 分片编号（从 1 开始）
     * @param file        分片文件数据
     * @return 分片上传结果
     */
    @PostMapping("/multipart/upload")
    public R<ChunkUploadVO> uploadChunk(
            @RequestParam String bucketName,
            @RequestParam String objectName,
            @RequestParam String uploadId,
            @RequestParam Integer chunkNumber,
            @RequestParam MultipartFile file) {

        ChunkUploadVO result = minioService.uploadChunk(bucketName, objectName, uploadId, chunkNumber, file);
        if (result.getSuccess()) {
            return R.ok("分片上传成功", result);
        }
        return R.fail("分片上传失败: " + result.getErrorMsg());
    }

    /**
     * 合并分片完成上传
     * 所有分片上传完成后调用此接口，合并成完整文件
     *
     * @param dto 合并请求参数
     * @return 上传结果（包含文件 URL、大小等信息）
     */
    @PostMapping("/multipart/complete")
    public R<MultipartUploadResultVO> completeMultipartUpload(@Valid @RequestBody MultipartUploadMergeDTO dto) {
        try {
            MultipartUploadResultVO result = minioService.completeMultipartUpload(dto);
            return R.ok("文件上传完成", result);
        } catch (Exception e) {
            return R.fail("合并分片失败: " + e.getMessage());
        }
    }

    /**
     * 取消分片上传
     * 用于取消未完成的分片上传任务，释放服务器资源
     *
     * @param bucketName 存储桶名称
     * @param objectName 文件对象名
     * @param uploadId   分片上传 ID
     * @return 操作结果
     */
    @DeleteMapping("/multipart/abort")
    public R<Void> abortMultipartUpload(
            @RequestParam String bucketName,
            @RequestParam String objectName,
            @RequestParam String uploadId) {

        minioService.abortMultipartUpload(bucketName, objectName, uploadId);
        return R.ok("已取消分片上传");
    }

    /**
     * 获取已上传的分片列表（用于断点续传）
     *
     * @param uploadId 分片上传 ID
     * @return 已上传的分片编号列表
     */
    @GetMapping("/multipart/uploaded-chunks")
    public R<List<Integer>> getUploadedChunks(@RequestParam String uploadId) {
        List<Integer> chunks = minioService.getUploadedChunks(uploadId);
        return R.ok(chunks);
    }

}
