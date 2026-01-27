package com.goalias.common.chat.entity.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 *  
 *
 * @author Goalias
 * @since 2026-01-22 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class Message extends BaseMessage implements Serializable {

    private Object content;
    @JsonProperty("reasoning_content")
    private String reasoningContent;

    public static Builder builder() {
        return new Builder();
    }

    public Message() {
    }

    private Message(Builder builder) {
        setContent(builder.content);
        super.setRole(builder.role);
        super.setName(builder.name);
    }

    public static final class Builder {
        private String role;
        private String content;
        private String name;

        public Builder() {
        }

        public Builder role(Role role) {
            this.role = role.getName();
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Message build() {
            return new Message(this);
        }
    }
}
