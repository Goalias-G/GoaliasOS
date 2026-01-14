package com.goalias.common.chat.entity.whisper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 *  
 *
 * @author Goalias
 * @since 2023-03-02
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhisperResponse implements Serializable {

    private String text;
}
