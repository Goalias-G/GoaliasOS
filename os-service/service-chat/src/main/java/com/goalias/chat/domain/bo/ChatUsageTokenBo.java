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
     * 用户
     */
    @NotNull(message = "用户不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;

    /**
     * 待结算token
     */
    @NotNull(message = "待结算token不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer token;

    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String modelName;

    /**
     * 累计使用token
     */
    @NotBlank(message = "累计使用token不能为空", groups = { AddGroup.class, EditGroup.class })
    private String totalToken;


}
