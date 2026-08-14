package com.goalias.system.domain.vo;

import lombok.Data;

/**
 * 上传对象信息
 *
 * @author Goalias
 */
@Data
public class SysOssUploadVo {

    /**
     * URL地址
     */
    private String url;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 对象存储主键
     */
    private Long ossId;
    /**
     * 文件大小
     */
    private Long fileSize;

}
