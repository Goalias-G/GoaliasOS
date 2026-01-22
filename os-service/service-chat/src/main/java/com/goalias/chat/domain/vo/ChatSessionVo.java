package com.goalias.chat.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;



/**
 * 会话管理视图对象 chat_session
 *
 * @author Goalias
 * @since 2026-01-22 */
@Data
public class ChatSessionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
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
     * 创建时间
     */
    private String createTime;

    /**
     * 会话id
     */
    private String conversationId;


}
