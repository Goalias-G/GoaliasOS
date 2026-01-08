package com.goalias.common.chat.entity.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import com.goalias.common.chat.entity.common.Usage;

import java.io.Serializable;
import java.util.List;

/**
 * chat答案类
 *
 * @author https:www.unfbx.com
 * 2023-03-02
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionResponse implements Serializable {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<ChatChoice> choices;
    private Usage usage;
}
