package com.goalias.domain.vo;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 配置信息视图对象 chat_config
 *
 * @author ageerle
 * @date 2025-04-08
 */
@Data
public class ChatConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 配置类型
     */
    private String category;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 说明
     */
    private String configDict;

    /**
     * 备注
     */
    private String remark;

    /**
     * 更新IP
     */
    private String updateIp;


}
