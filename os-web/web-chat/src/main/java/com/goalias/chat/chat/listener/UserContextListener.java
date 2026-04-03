package com.goalias.chat.chat.listener;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.goalias.chat.chat.event.UserContextUpdateEvent;
import com.goalias.chat.chat.service.ISseService;
import com.goalias.chat.enums.PromptTemplateEnum;
import com.goalias.common.chat.request.ChatRequest;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserContextListener {

    private final RedisService redisService;

    private final ISseService sseService;

    @Async
    @EventListener
    public void onUserContextUpdate(@NotNull UserContextUpdateEvent event) {
        log.debug("UserContextListener->接收到用户画像更新事件，用户ID: {}，原始描述: {}",
                event.getUserId(), event.getDescribe());
        Map<String, Object> currentUserContext = redisService.hmGet(CacheNames.CHAT_USER_CONTEXT + event.getUserId());
        String chatJson = sseService.simpleChat(new ChatRequest(), PromptTemplateEnum.USER_CONTEXT, currentUserContext, event.getDescribe());
        boolean isJson = JSONUtil.isTypeJSON(chatJson);
        if (isJson) {
            JSONObject userJson = JSONUtil.parseObj(chatJson);
            JSONArray jsonArray = userJson.getJSONArray("result");
            if (Objects.nonNull(jsonArray) && !jsonArray.isEmpty()) {
                Map<String, String> userMap = new HashMap<>();
                jsonArray.forEach(fieldObj -> {
                    JSONObject jsonObject = JSONUtil.parseObj(fieldObj);
                    userMap.put(jsonObject.getStr("field"), jsonObject.getStr("value"));
                });
                redisService.hmSet(CacheNames.CHAT_USER_CONTEXT + event.getUserId(), userMap);
            }

        }
    }
}

