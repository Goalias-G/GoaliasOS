package com.goalias.common.oss.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分片上传结果响应 VO
 * 合并完成后返回给前端的文件信息
 *
 * @author Goalias
 */
@Data
@Builder
public class MultipartUploadResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文件名（包含路径）
     */
    private String objectName;

    /**
     * 文件访问 URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件 ETag（可用于校验）
     */
    private String etag;

}
