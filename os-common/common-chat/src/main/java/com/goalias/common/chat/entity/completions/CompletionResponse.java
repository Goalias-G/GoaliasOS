package com.goalias.common.chat.entity.completions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import com.goalias.common.chat.entity.common.Choice;
import com.goalias.common.chat.entity.common.OpenAiResponse;
import com.goalias.common.chat.entity.common.Usage;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 *   答案类
 *
 * @author Goalias
 *  2023-02-11
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = false)
public class CompletionResponse extends OpenAiResponse implements Serializable {
    private String id;
    private String object;
    private long created;
    private String model;
    private Choice[] choices;
    private Usage usage;
}
