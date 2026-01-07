package com.goalias.common.oss.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分片上传合并请求 DTO
 * 所有分片上传完成后，调用合并接口完成文件上传
 *
 * @author Goalias
 */
@Data
public class MultipartUploadMergeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 存储桶名称
     */
    @NotBlank(message = "存储桶名称不能为空")
    private String bucketName;

    /**
     * 文件名（包含路径）
     */
    @NotBlank(message = "文件名不能为空")
    private String objectName;

    /**
     * 分片上传 ID（初始化时返回）
     */
    @NotBlank(message = "上传ID不能为空")
    private String uploadId;

}
