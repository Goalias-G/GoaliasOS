package com.goalias.chat.chat.service;

import jakarta.servlet.http.HttpServletRequest;
import com.goalias.common.chat.entity.Tts.TextToSpeech;
import com.goalias.common.chat.entity.files.UploadFileResponse;
import com.goalias.common.chat.entity.whisper.WhisperResponse;
import com.goalias.common.chat.request.ChatRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


/**
 * 用户聊天管理Service接口
 *
 * @author Goalias
 * @since 2026-01-22 */
public interface ISseService {

    /**
     * 客户端发送消息到服务端
     * @param chatRequest 请求对象
     */
    SseEmitter sseChat(ChatRequest chatRequest, HttpServletRequest request);

    /**
     * 语音转文字
     * @param file 语音文件
     */
   WhisperResponse speechToTextTranscriptionsV2(MultipartFile file);

    /**
     * 文字转语音
     *
     * @param textToSpeech 文本信息
     * @return 流式语音
     */
    ResponseEntity<Resource> textToSpeed(TextToSpeech textToSpeech);

    /**
     * 上传文件到服务器
     *
     * @param file 文件信息
     * @return 返回文件信息
     */
    UploadFileResponse upload(MultipartFile file);


}
