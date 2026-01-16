package com.goalias.chat.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;




/**
 * 用户token使用详情视图对象 chat_usage_token
 *
 * @author Goalias
 * @since 2026-01-16
 */
@Data
public class ChatUsageTokenVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 用户
     */
    private Long userId;

    /**
     * 待结算token
     */
    private Integer token;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 累计使用token
     */
    private String totalToken;


}
