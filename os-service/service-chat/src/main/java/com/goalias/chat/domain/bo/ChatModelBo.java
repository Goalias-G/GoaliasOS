package com.goalias.chat.domain.bo;

import com.goalias.common.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.core.validate.EditGroup;

/**
 * 聊天模型业务对象 chat_model
 *
 * @author Goalias
 * @since 2026-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatModelBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 模型分类
     */
    @NotBlank(message = "模型分类不能为空", groups = { AddGroup.class, EditGroup.class })
    private String category;

    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String modelName;

    /**
     * 模型描述
     */
    @NotBlank(message = "模型描述不能为空", groups = { AddGroup.class, EditGroup.class })
    private String modelDescribe;

    /**
     * 模型价格
     */
    @NotNull(message = "模型价格不能为空", groups = { AddGroup.class, EditGroup.class })
    private Double modelPrice;

    /**
     * 计费类型
     */
    @NotBlank(message = "计费类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String modelType;

    /**
     * 是否显示
     */
    @NotBlank(message = "是否显示不能为空", groups = { AddGroup.class, EditGroup.class })
    private String modelShow;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 请求地址
     */
    @NotBlank(message = "请求地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String apiHost;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 密钥
     */
    @NotBlank(message = "密钥不能为空", groups = { AddGroup.class, EditGroup.class })
    private String apiKey;

    /**
     * 模型供应商
     */
    private String ProviderName;

    /**
     * 备注
     */
    @NotBlank(message = "备注不能为空", groups = { AddGroup.class, EditGroup.class })
    private String remark;


}
