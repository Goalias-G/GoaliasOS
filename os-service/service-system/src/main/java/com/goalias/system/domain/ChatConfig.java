package com.goalias.system.domain;


import com.baomidou.mybatisplus.annotation.*;
import com.goalias.common.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 配置信息对象 chat_config
 *
 * @author Goalias
 * @since 2026-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_config")
public class ChatConfig extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * 版本
     */
    @Version
    private Long version;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    @TableLogic
    private String delFlag;

    /**
     * 更新IP
     */
    private String updateIp;


}
