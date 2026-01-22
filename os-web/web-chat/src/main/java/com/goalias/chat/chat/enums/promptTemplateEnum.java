package com.goalias.chat.chat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提示词模板分类
 *
 * @author Goalias
 */
@Getter
@AllArgsConstructor
public enum promptTemplateEnum {
    CHAT(1, "chat"),
    VECTOR(2, "vector"),
    ;

    private final Integer code;
    private final String desc;

}

