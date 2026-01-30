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
    SUMMARY(3, "summary"),
    ;

    private final Integer code;
    private final String desc;

}

