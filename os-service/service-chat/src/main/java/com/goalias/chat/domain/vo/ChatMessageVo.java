package com.goalias.chat.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * 聊天消息视图对象 chat_message
 *
 * @author Goalias
 * @since 2026-01-16
 */
@Data
public class ChatMessageVo implements Serializable {

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
     * 会话id
     */
    private Long sessionId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 对话角色
     */
    private String role;

    /**
     * 扣除金额
     */
    private BigDecimal deductCost;

    /**
     * 累计 Tokens
     */
    private Long totalTokens;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 计费类型（1-token计费，2-次数计费）
     */
    private String billingType;

    /**
     * 备注
     */
    private String remark;


    /**
     * 创建时间
     */
    private Date createTime;



}
