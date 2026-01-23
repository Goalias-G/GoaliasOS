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

    @NotEmpty(message = "传入的模型不能为空")
    private String model;

    /**
     * 提示词(自动填充)
     */
    private String prompt;


    /**
     * 系统提示词(自动填充)
     */
    private String sysPrompt;


    /**
     * 消息id
     */
    private Long messageId;

    /**
     * 是否开启流式对话
     */
    private Boolean stream = Boolean.TRUE;

    /**
     * 知识库id
     */
    private String kid;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 会话id
     */
    private Long sessionId;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 对话角色
     */
    private String role;


    /**
     * 对话id(每个聊天窗口都不一样)
     */
    private Long uuid;

    /**
     * 是否有附件
     */
    private Boolean hasAttachment;

    /**
     * 是否启用深度思考
     */
    private Boolean enableThinking;

    /**
     * 是否自动切换模型
     */
    private Boolean autoSelectModel;

    /**
     * 会话令牌（为避免在非Web线程中获取Request，入口处注入）
     */
    private String token;

    /**
     * 采样温度，取值范围[0,2]
     * 控制随机性强度
     */
    private Float temperature = 0.7f;

    /**
     * 模型采样概率，取值范围[0,1]
     * P=0.1 → 高确定性（只选高概率词）
     * P=0.9 → 高多样性（包含更多低概率词）
     */
    private Double topP = 0.9;

}
