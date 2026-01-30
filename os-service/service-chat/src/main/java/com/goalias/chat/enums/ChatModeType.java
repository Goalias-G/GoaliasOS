package com.goalias.chat.enums;

import lombok.Getter;

@Getter
public enum ChatModeType {

    QIANWEN("alibailian", "通义千问"),

    VECTOR("vector", "知识库向量模型"),

    IMAGE("image", "图片识别模型"),

    SUMMARY("summary", "提炼/总结文本模型"),
    ;

    private final String code;
    private final String description;

    ChatModeType(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
