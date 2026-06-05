package com.goalias.chat.schedule.executor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.goalias.chat.service.ISseService;
import com.goalias.common.chat.entity.chat.Message;
import com.goalias.common.chat.request.ChatRequest;
import com.goalias.common.core.exception.ServiceException;
import com.goalias.common.notification.core.MailTemplate;
import com.goalias.common.schedule.enums.TaskType;
import com.goalias.common.schedule.executor.AbstractTaskExecutor;
import com.goalias.common.schedule.executor.TaskExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * AI 对话并邮件下发的任务执行器
 * <p>
 * 流程：
 * <ol>
 *     <li>解析 params 中的 prompt / recipient / subject</li>
 *     <li>构造 {@link ChatRequest}，以 user 角色将 prompt 发给 AI</li>
 *     <li>调用 {@link ISseService#simpleChat(ChatRequest, com.goalias.chat.enums.PromptTemplateEnum, Object...)} 拉取回复</li>
 *     <li>将回复作为邮件正文（简单文本）发送至 recipient</li>
 * </ol>
 * AI 调用失败时仍向 recipient 发送一封失败通知邮件，便于用户感知。
 *
 * @author Goalias
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailChatTaskExecutor extends AbstractTaskExecutor {

    /** prompt 文本 */
    public static final String PARAM_PROMPT = "prompt";
    /** 收件人邮箱，多个以英文逗号分隔 */
    public static final String PARAM_RECIPIENT = "recipient";
    /** 邮件主题，可选 */
    public static final String PARAM_SUBJECT = "subject";

    private static final DateTimeFormatter SUBJECT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String DEFAULT_SUBJECT_PREFIX = "Goalias OS 提醒 ";
    private static final String FAILURE_SUBJECT_PREFIX = "[失败] ";

    private final ISseService sseService;
    private final MailTemplate mailTemplate;

    @Override
    public TaskType supports() {
        return TaskType.EMAIL_CHAT;
    }

    @Override
    protected void doExecute(TaskExecutionContext context) {
        Map<String, Object> params = context.getParams();
        if (CollUtil.isEmpty(params)) {
            throw new ServiceException("EMAIL_CHAT 任务缺少自定义参数");
        }
        String prompt = toString(params.get(PARAM_PROMPT));
        if (prompt == null || prompt.isBlank()) {
            throw new ServiceException("EMAIL_CHAT 任务必须传入 params.prompt");
        }
        String recipient = toString(params.get(PARAM_RECIPIENT));
        if (recipient == null || recipient.isBlank()) {
            throw new ServiceException("EMAIL_CHAT 任务必须传入 params.recipient");
        }
        String subject = toString(params.get(PARAM_SUBJECT));
        if (subject == null || subject.isBlank()) {
            subject = DEFAULT_SUBJECT_PREFIX + LocalDateTime.now().format(SUBJECT_FORMAT);
        }

        // 构造 AI 请求：以 user 角色发送 prompt
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setUserId(context.getUserId());
        chatRequest.setIsJsonResponse(false);
        chatRequest.setMessages(List.of(
            Message.builder().role(Message.Role.USER).content(prompt).build()
        ));

        String aiResponse;
        try {
            aiResponse = sseService.simpleChat(chatRequest, null);
        } catch (Exception aiEx) {
            log.error("[EmailChat 任务] AI 调用失败 taskId={}", context.getTaskId(), aiEx);
            // 发送失败通知邮件
            try {
                String failureBody = buildFailureBody(context.getTaskId(), prompt, aiEx);
                mailTemplate.sendSimpleMail(recipient, FAILURE_SUBJECT_PREFIX + subject, failureBody);
            } catch (Exception mailEx) {
                log.error("[EmailChat 任务] 失败通知邮件发送失败 taskId={}", context.getTaskId(), mailEx);
            }
            throw new ServiceException("AI 调用失败: " + aiEx.getMessage());
        }

        // 发送 AI 回复邮件
        try {
            mailTemplate.sendSimpleMail(recipient, subject, ObjectUtil.defaultIfEmpty(aiResponse, "(空响应)"));
        } catch (Exception mailEx) {
            log.error("[EmailChat 任务] 邮件发送失败 taskId={} recipient={}", context.getTaskId(), recipient, mailEx);
            throw new ServiceException("邮件发送失败: " + mailEx.getMessage());
        }
    }

    private String buildFailureBody(Long taskId, String prompt, Throwable aiEx) {
        return "AI 调用失败，无法生成回复。\n"
            + "任务ID：" + (taskId == null ? "-" : taskId) + "\n"
            + "提示词：" + prompt + "\n"
            + "原因：" + aiEx.getClass().getSimpleName() + ": " + aiEx.getMessage();
    }

    private String toString(Object value) {
        return value == null ? null : value.toString();
    }
}
