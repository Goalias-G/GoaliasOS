package com.goalias.common.chat.entity.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;

/**
 *
 * @author Goalias
 * @since 2026-01-22 * 2023-03-02
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class BaseMessage implements Serializable {

    /**
     * 目前支持四个中角色参考官网，进行情景输入：
     * https://platform.openai.com/docs/guides/chat/introduction
     */
    private String role;


    private String name;

    public BaseMessage() {
    }


    @Getter
    @AllArgsConstructor
    public enum Role {

        SYSTEM("system"),
        USER("user"),
        ASSISTANT("assistant"),
        FUNCTION("function"),
        TOOL("tool"),
        ;
        private final String name;
    }

}
