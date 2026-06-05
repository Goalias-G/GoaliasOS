package com.goalias.common.schedule.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务类型枚举
 * <p>
 * 与策略模式一一对应：新增任务类型需同步扩展 {@link com.goalias.common.schedule.executor.TaskExecutorStrategy} 实现。
 * <p>
 * 各类型约定的 {@code TaskExecutionContext.params} 自定义参数：
 * <ul>
 *     <li><b>FINANCE</b>：财务记账
 *         <ul>
 *             <li>categoryId: Long，必填，财务分类ID（finance_category.id）</li>
 *             <li>amount: Long，可选，默认 0，金额（分）</li>
 *             <li>tag: Integer，可选，默认 2，流水标签（1-必要支出 2-弹性支出 3-工薪收入 4-额外收入）</li>
 *             <li>remark: String，可选，备注</li>
 *         </ul>
 *     </li>
 *     <li><b>EMAIL_CHAT</b>：AI 对话并邮件下发
 *         <ul>
 *             <li>prompt: String，必填，作为用户原文发送给 AI</li>
 *             <li>recipient: String，必填，收件人邮箱（多个以英文逗号分隔）</li>
 *             <li>subject: String，可选，邮件主题，默认 "Goalias OS 提醒 yyyy-MM-dd HH:mm"</li>
 *         </ul>
 *         AI 调用失败时仍会向 recipient 发送一封失败通知邮件。
 *     </li>
 * </ul>
 *
 * @author Goalias
 */
@Getter
@AllArgsConstructor
public enum TaskType {

    /**
     * 财务模块任务
     */
    FINANCE("FINANCE", "财务任务"),

    /**
     * AI 对话并将结果邮件下发的任务
     */
    EMAIL_CHAT("EMAIL_CHAT", "AI对话邮件任务");

    private final String code;
    private final String desc;

    /**
     * 通过字符串代码解析任务类型，未匹配时返回 null
     */
    public static TaskType of(String code) {
        if (code == null) {
            return null;
        }
        for (TaskType value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
