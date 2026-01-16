package com.goalias.chat.domain.bo;

import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.core.validate.EditGroup;
import com.goalias.common.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 聊天消息业务对象 chat_message
 *
 * @author Goalias
 * @since 2026-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatMessageBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 用户id
     */
    @NotNull(message = "用户id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空", groups = { AddGroup.class, EditGroup.class })
    private String content;

    /**
     * 会话id
     */
    @NotBlank(message = "会话id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long sessionId;

    /**
     * 对话角色
     */
    @NotBlank(message = "对话角色不能为空", groups = { AddGroup.class, EditGroup.class })
    private String role;

    /**
     * 扣除金额
     */
    @NotNull(message = "扣除金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private Double deductCost;

    /**
     * 累计 Tokens
     */
    @NotNull(message = "累计 Tokens不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer totalTokens;

    /**
     * 模型名称
     */
    @NotBlank(message = "模型名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String modelName;

    /**
     * 备注
     */
    @NotBlank(message = "备注不能为空", groups = { AddGroup.class, EditGroup.class })
    private String remark;

    /**
     * 计费类型（1-token计费，2-次数计费，null-普通消息）
     */
    private String billingType;


}
