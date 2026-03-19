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
    ;

    private final Integer code;
    private final String desc;

}

