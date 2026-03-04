package com.goalias.common.chat.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import com.goalias.common.chat.entity.chat.Message;

import java.util.List;

/**
 * 对话请求对象
 *
 * @author Goalias
 * @sine 2023-04-08
 */
@Data
public class ChatRequest {

    @NotEmpty(message = "对话消息不能为空")
    List<Message> messages;

    private String model;

    /**
     * 会话id
     */
    private Long sessionId;

    /**
     * 知识库id
     */
    private String kid;

    /**
     * 是否联网搜索(若支持)(0-否 1-是)
     */
    private Boolean enableSearch =  false;

    /**
     * 是否自动切换模型
     */
    private Boolean autoSelectModel;

    /**
     * 是否有附件
     */
    private Boolean hasAttachment;

    /**
     * 采样温度，取值范围[0,2]
     * 控制随机性强度
     */
    private Double temperature = 0.7;

    /**
     * 模型采样概率，取值范围[0,1]
     * P=0.1 → 高确定性（只选高概率词）
     * P=0.9 → 高多样性（包含更多低概率词）
     */
    private Double topP = 0.9;

//    -----------------------------

    /**
     * 消息id
     */
    private Long messageId;

    /**
     * 用户id
     */
    private Long userId;


    /**
     * 对话角色
     */
    private String role;


    /**
     * 会话令牌（为避免在非Web线程中获取Request，入口处注入）
     */
    private String token;

    /**
     * 提示词(自动填充)
     */
    private String prompt;


    /**
     * 系统提示词(自动填充)
     */
    private String sysPrompt;

}
