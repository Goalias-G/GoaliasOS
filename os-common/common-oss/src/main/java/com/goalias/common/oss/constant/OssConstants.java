package com.goalias.common.oss.constant;

/**
 * OSS 相关常量定义
 *
 * @author Goalias
 */
public interface OssConstants {

    /**
     * 分片上传相关常量
     */
    interface Multipart {
        /**
         * 默认分片大小：5MB（MinIO 最小分片要求）
         */
        long DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024L;

        /**
         * 最小分片大小：5MB
         */
        long MIN_CHUNK_SIZE = 5 * 1024 * 1024L;

        /**
         * 最大分片大小：5GB
         */
        long MAX_CHUNK_SIZE = 5L * 1024 * 1024 * 1024;

        /**
         * 最大分片数量：10000
         */
        int MAX_CHUNK_COUNT = 10000;

        /**
         * 预签名 URL 有效期（秒）：1小时 前端直传minio使用
         */
        int PRESIGNED_URL_EXPIRY = 3600;
    }

    /**
     * Redis 缓存 Key 前缀
     */
    interface CacheKey {
        /**
         * 分片上传信息缓存前缀
         * 完整 key: oss:multipart:upload:{uploadId}
         */
        String MULTIPART_UPLOAD_PREFIX = "oss:multipart:upload:";

        /**
         * 文件 MD5 映射缓存前缀（用于秒传）
         * 完整 key: oss:file:md5:{bucketName}:{md5}
         */
        String FILE_MD5_PREFIX = "oss:file:md5:";

        /**
         * 已上传分片记录前缀
         * 完整 key: oss:multipart:chunks:{uploadId}
         */
        String UPLOADED_CHUNKS_PREFIX = "oss:multipart:chunks:";
    }

    /**
     * 缓存过期时间（秒）
     */
    interface CacheExpire {
        /**
         * 分片上传信息缓存过期时间：24小时
         */
        long MULTIPART_UPLOAD_EXPIRE = 24 * 60 * 60L;

        /**
         * 文件 MD5 映射缓存过期时间：7天
         */
        long FILE_MD5_EXPIRE = 7 * 24 * 60 * 60L;
    }

}
