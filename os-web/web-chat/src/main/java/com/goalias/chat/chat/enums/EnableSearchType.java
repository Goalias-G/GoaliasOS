package com.goalias.chat.chat.enums;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum EnableSearchType {
    YES(1, "联网搜索"),
    NO(0, "模型回复");

    private final Integer code;
    private final String description;

    EnableSearchType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static EnableSearchType fromCode(Integer code) {
        for (EnableSearchType type : values()) {
            if (Objects.equals(type.code, code)) {
                return type;
            }
        }
        return null;
    }

}
