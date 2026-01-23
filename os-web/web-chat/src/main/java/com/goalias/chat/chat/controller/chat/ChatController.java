package com.goalias.chat.chat.controller.chat;


import com.goalias.chat.chat.service.ISseService;
import com.goalias.common.chat.entity.Tts.TextToSpeech;
import com.goalias.common.chat.entity.whisper.WhisperResponse;
import com.goalias.common.chat.request.ChatRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


/**
 *  聊天管理(工厂实现类模型chat)
 *
 * @author Goalias
 * @since 2026-01-22 */
@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ISseService sseService;

    /**
     * 聊天接口
     */
    @PostMapping("/send")
    @ResponseBody
    public SseEmitter sseChat(@RequestBody @Valid ChatRequest chatRequest, HttpServletRequest request) {
        return sseService.sseChat(chatRequest,request);
    }


    /**
     * 语音转文本
     *
     * @param file
     */
    @PostMapping("/audio")
    @ResponseBody
    public WhisperResponse audio(@RequestParam("file") MultipartFile file) {
//        return sseService.speechToTextTranscriptionsV2(file);
        return null;
    }


    /**
     * 文本转语音
     *
     * @param textToSpeech
     */
    @PostMapping("/speech")
    @ResponseBody
    public ResponseEntity<Resource> speech(@RequestBody TextToSpeech textToSpeech) {
//        return sseService.textToSpeed(textToSpeech);
        return null;
    }

}
