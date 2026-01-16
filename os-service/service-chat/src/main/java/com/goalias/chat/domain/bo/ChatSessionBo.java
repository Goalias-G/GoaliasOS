package com.goalias.chat.domain.bo;

import com.goalias.common.web.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.goalias.common.core.validate.EditGroup;

/**
 * 会话管理业务对象 chat_session
 *
 * @author Goalias
 * @since 2026-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatSessionBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 会话标题
     */
    private String sessionTitle;

    /**
     * 会话内容
     */
    private String sessionContent;

    /**
     * 备注
     */
    private String remark;
    /**
     * 会话id
     */
    private String conversationId;

}
