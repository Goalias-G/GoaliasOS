package com.goalias.runner;

import com.goalias.chat.domain.ChatUsageToken;
import com.goalias.chat.mapper.ChatUsageTokenMapper;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应用启动Runner
 *
 * @author Goalias
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class TokenUsageRunner implements ApplicationRunner {

    private final ChatUsageTokenMapper baseMapper;
    private final RedisService redisService;


    @Override
    public void run(ApplicationArguments args) {
        loadTokenToRedis();
        log.info("GoaliasOS start successfully!");
    }

    /**
     * 程序启动时加载数据库数据到Redis
     */
    public void loadTokenToRedis() {
        log.info("开始从数据库加载Token使用量到Redis...");
        try {
            List<ChatUsageToken> tokenList = baseMapper.selectList(null);
            if (tokenList != null && !tokenList.isEmpty()) {
                Map<String, Object> inputTokenMap = new HashMap<>();
                Map<String, Object> outputTokenMap = new HashMap<>();

                for (ChatUsageToken token : tokenList) {
                    String modelName = token.getModelName();
                    if (modelName != null) {
                        if (token.getInputToken() != null) {
                            inputTokenMap.put(modelName, token.getInputToken());
                        }
                        if (token.getOutputToken() != null) {
                            outputTokenMap.put(modelName, token.getOutputToken());
                        }
                    }
                }

                if (!inputTokenMap.isEmpty()) {
                    redisService.hmSet(CacheNames.CHAT_TOKEN_INPUT, inputTokenMap);
                }
                if (!outputTokenMap.isEmpty()) {
                    redisService.hmSet(CacheNames.CHAT_TOKEN_OUTPUT, outputTokenMap);
                }
                log.info("成功加载 {} 条Token使用记录到Redis", tokenList.size());
            } else {
                log.info("数据库中没有Token使用记录");
            }
        } catch (Exception e) {
            log.error("从数据库加载Token使用量到Redis失败", e);
        }
    }

}
