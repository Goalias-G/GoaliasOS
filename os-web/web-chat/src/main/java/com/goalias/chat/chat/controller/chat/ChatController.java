package com.goalias.chat.chat.controller.chat;


import cn.hutool.json.JSONUtil;
import com.goalias.chat.chat.service.ISseService;
import com.goalias.common.chat.entity.Tts.TextToSpeech;
import com.goalias.common.chat.entity.whisper.WhisperResponse;
import com.goalias.common.chat.request.ChatRequest;
import com.goalias.common.core.domain.R;
import com.goalias.common.rateLimiter.annotation.GoaliasFallback;
import com.goalias.common.rateLimiter.enums.FlowGradeEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;


/**
 *  聊天管理(工厂实现类模型chat)
 *
 * @author Goalias
 * @since 2026-01-22 */
@RestController
@Slf4j
@RequestMapping("/chat")
public class ChatController {

    @jakarta.annotation.Resource
    private ISseService sseService;

    /**
     * 聊天接口
     */
    @PostMapping("/send")
    @GoaliasFallback(grade = FlowGradeEnum.FLOW_GRADE_QPS, count = 5)
    public SseEmitter sseChat(@RequestBody @Valid ChatRequest chatRequest, HttpServletRequest request) {
        return sseService.sseChat(chatRequest,request);
    }
    public void sseChatFallback(HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream;charset=UTF-8");
        R<Object> fail = R.fail("不好意思，刚才与 Goalias AI 交流的人太多了，请让我休息下稍后再试~");
        response.getWriter().write(JSONUtil.toJsonStr(fail));
        response.getWriter().flush();
        log.info("fallback sseChat");
    }

    /**
     * 语音转文本
     * @param file
     */
    @PostMapping("/audio")
    public WhisperResponse audio(@RequestParam("file") MultipartFile file) {
//        return sseService.speechToTextTranscriptionsV2(file);
        return null;
    }


    /**
     * 文本转语音
     * @param textToSpeech
     */
    @PostMapping("/speech")
    public ResponseEntity<Resource> speech(@RequestBody TextToSpeech textToSpeech) {
//        return sseService.textToSpeed(textToSpeech);
        return null;
    }

}
