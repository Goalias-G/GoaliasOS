package com.goalias.chat.chat.controller.chat;


import cn.hutool.json.JSONUtil;
import com.goalias.chat.service.ISseService;
import com.goalias.common.chat.request.ChatRequest;
import com.goalias.common.core.domain.R;
import com.goalias.common.rateLimiter.annotation.GoaliasFallback;
import com.goalias.common.rateLimiter.enums.FlowGradeEnum;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;


/**
 * 聊天管理(工厂实现类模型chat)
 *
 * @author Goalias
 * @since 2026-01-22
 */
@RestController
@Slf4j
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ISseService sseService;

    /**
     * 聊天接口
     */
    @PostMapping("/send")
    @GoaliasFallback(grade = FlowGradeEnum.FLOW_GRADE_QPS, count = 5)
    public SseEmitter sseChat(@RequestBody @Valid ChatRequest chatRequest) {
        return sseService.sseChat(chatRequest);
    }

    @SuppressWarnings("unused")
    public SseEmitter sseChatFallback(ChatRequest chatRequest) throws IOException {
        log.warn("fallback sseChat");
        SseEmitter sseEmitter = new SseEmitter(0L);
        sseEmitter.send("不好意思，刚才与 Goalias AI 交流的人太多了，请让我休息下稍后再试~");
//        response.setContentType("text/event-stream;charset=UTF-8");
//        R<Object> fail = R.fail("不好意思，刚才与 Goalias AI 交流的人太多了，请让我休息下稍后再试~");
//        response.getWriter().write(JSONUtil.toJsonStr(fail));
//        response.getWriter().flush();
        return sseEmitter;
    }

    @PostMapping("/simple")
    public R<String> simpleChat(@RequestBody @Valid ChatRequest chatRequest) {
        String result = sseService.simpleChat(chatRequest, null);
        return R.ok(result);
    }


}
