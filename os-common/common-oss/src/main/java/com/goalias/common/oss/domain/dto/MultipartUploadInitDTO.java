package com.goalias.common.oss.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分片上传初始化请求 DTO
 * 前端在开始分片上传前，需要先调用初始化接口获取 uploadId
 *
 * @author Goalias
 */
@Data
public class MultipartUploadInitDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 存储桶名称
     */
    @NotBlank(message = "存储桶名称不能为空")
    private String bucketName;

    /**
     * 文件名（包含路径，如：upload/2026/01/07/test.mp4）
     */
    @NotBlank(message = "文件名不能为空")
    private String objectName;

    /**
     * 文件大小（字节）
     */
    @NotNull(message = "文件大小不能为空")
    @Positive(message = "文件大小必须大于0")
    private Long fileSize;

    /**
     * 文件 MD5 值（用于秒传校验，可选）
     */
    private String fileMd5;

    /**
     * 文件 MIME 类型（如：video/mp4）
     */
    private String contentType;

    /**
     * 分片大小（字节），默认 5MB
     */
    private Long chunkSize = 5 * 1024 * 1024L;

}
