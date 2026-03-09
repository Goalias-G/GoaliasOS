package com.goalias.chat.domain.bo;

import com.goalias.common.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.core.validate.EditGroup;


/**
 * 用户token使用详情业务对象 chat_usage_token
 *
 * @author Goalias
 * @since 2026-01-22 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatUsageTokenBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String modelName;

    /**
     * 模型输入token
     */
    @NotNull(message = "模型输入token不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long inputToken;

    /**
     * 输出token
     */
    @NotNull(message = "输出token不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long outputToken;


}
