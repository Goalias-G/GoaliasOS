package com.goalias.common.oss.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分片上传初始化响应 VO
 * 返回给前端用于后续分片上传的必要信息
 *
 * @author Goalias
 */
@Data
@Builder
public class MultipartUploadInitVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分片上传 ID（后续上传分片和合并时需要）
     */
    private String uploadId;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 文件名（包含路径）
     */
    private String objectName;

    /**
     * 总分片数
     */
    private Integer totalChunks;

    /**
     * 每个分片的大小（字节）
     */
    private Long chunkSize;

    /**
     * 是否秒传成功（文件已存在）
     */
    private Boolean instantUpload;

    /**
     * 秒传成功时的文件访问 URL
     */
    private String fileUrl;

    /**
     * 已上传的分片编号列表（用于断点续传）
     */
    private List<Integer> uploadedChunks;

    /**
     * 各分片的预签名上传 URL 列表
     * 前端可直接使用这些 URL 上传分片到 MinIO，无需经过后端
     */
    private List<String> uploadUrls;

}
