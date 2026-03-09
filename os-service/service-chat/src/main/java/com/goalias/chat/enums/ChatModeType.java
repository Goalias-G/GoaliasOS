package com.goalias.chat.enums;

import lombok.Getter;

@Getter
public enum ChatModeType {

    QIANWEN("alibailian", "通义千问"),

    GLM("glm", "GLM智谱模型"),

    IMAGE("image", "图片识别模型"),
    ;

    private final String code;
    private final String description;

    ChatModeType(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
