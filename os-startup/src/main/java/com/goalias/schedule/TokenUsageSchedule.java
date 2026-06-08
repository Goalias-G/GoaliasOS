package com.goalias.schedule;

import com.goalias.chat.domain.ChatUsageToken;
import com.goalias.chat.mapper.ChatUsageTokenMapper;
import com.goalias.chat.service.IChatUsageTokenService;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 应用启动Runner
 *
 * @author Goalias
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Profile("prod")
public class TokenUsageSchedule {

    private final IChatUsageTokenService usageTokenService;

    private final ChatUsageTokenMapper baseMapper;

    private final RedisService redisService;


    /**
     * 每6小时同步Redis数据到数据库
     */
    @Scheduled(fixedRate = 12, timeUnit = TimeUnit.HOURS)
    public void syncToDatabase() {
        log.info("开始同步Redis Token使用量到数据库...");
        try {
            Map<String, Object> inputTokenMap = redisService.hmGet(CacheNames.CHAT_TOKEN_INPUT);
            Map<String, Object> outputTokenMap = redisService.hmGet(CacheNames.CHAT_TOKEN_OUTPUT);

            if (inputTokenMap == null) {
                inputTokenMap = new java.util.HashMap<>();
            }
            if (outputTokenMap == null) {
                outputTokenMap = new java.util.HashMap<>();
            }

            Set<String> modelNames = new HashSet<>(inputTokenMap.keySet());
            modelNames.addAll(outputTokenMap.keySet());

            LocalDateTime now = LocalDateTime.now();
            int syncCount = 0;

            for (String modelName : modelNames) {
                if (modelName == null || modelName.isEmpty()) {
                    continue;
                }

                Long inputToken = inputTokenMap.get(modelName) != null
                        ? Long.parseLong(inputTokenMap.get(modelName).toString())
                        : 0L;
                Long outputToken = outputTokenMap.get(modelName) != null
                        ? Long.parseLong(outputTokenMap.get(modelName).toString())
                        : 0L;

                ChatUsageToken existingToken = usageTokenService.queryByModelName(modelName);
                if (existingToken != null) {
                    existingToken.setInputToken(inputToken);
                    existingToken.setOutputToken(outputToken);
                    existingToken.setUpdateTime(now);
                    baseMapper.updateById(existingToken);
                } else {
                    ChatUsageToken newToken = new ChatUsageToken();
                    newToken.setModelName(modelName);
                    newToken.setInputToken(inputToken);
                    newToken.setOutputToken(outputToken);
                    newToken.setUpdateTime(now);
                    baseMapper.insert(newToken);
                }
                syncCount++;
            }

            log.info("成功同步 {} 条Token使用记录到数据库", syncCount);
        } catch (Exception e) {
            log.error("同步Redis Token使用量到数据库失败", e);
        }
    }

}
