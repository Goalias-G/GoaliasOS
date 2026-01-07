package com.goalias.common.oss.domain.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分片上传响应 VO
 * 单个分片上传完成后的响应信息
 *
 * @author Goalias
 */
@Data
@Builder
public class ChunkUploadVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分片编号（从 1 开始）
     */
    private Integer chunkNumber;

    /**
     * 分片 ETag（MinIO 返回，合并时需要）
     */
    private String etag;

    /**
     * 是否上传成功
     */
    private Boolean success;

    /**
     * 错误信息（上传失败时）
     */
    private String errorMsg;

}
