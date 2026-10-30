package com.goalias.chat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提示词模板分类
 *
 * @author Goalias
 */
@Getter
@AllArgsConstructor
public enum PromptTemplateEnum {
    CHAT(1, "chat"),
    KNOWLEDGE(2, "knowledge"),
    USER_CONTEXT(3, "userContext"),
    AI_RECOMMEND(4, "aiRecommend"),
    MONTHLY_KERNEL_REVIEW(5, "monthlyKernelReview"),
    SESSION_RENAME(6, "sessionRename"),
    LIFE_RECORD(7, "lifeRecord"),
    REFLECTION(8, "reflection"),
    GOAL(9, "goal"),
    HABIT(10, "habit"),
    PORTRAIT(11, "portrait"),
    ;

    private final Integer code;
    private final String desc;

    /**
     * 按 desc（DB category）匹配枚举，未命中返回 null
     */
    public static PromptTemplateEnum fromDesc(String desc) {
        if (desc == null || desc.isBlank()) {
            return null;
        }
        for (PromptTemplateEnum value : values()) {
            if (value.desc.equals(desc)) {
                return value;
            }
        }
        return null;
    }

}

