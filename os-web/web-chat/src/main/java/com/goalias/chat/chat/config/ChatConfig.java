package com.goalias.chat.chat.config;

import com.goalias.common.chat.openai.OpenAiStreamClient;
import com.goalias.common.chat.openai.function.KeyRandomStrategy;
import com.goalias.common.chat.openai.interceptor.OpenAILogger;
import com.goalias.chat.service.IChatConfigService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Chat配置类
 *
 * @since 2026-01-22 */
@Configuration
@RequiredArgsConstructor
public class ChatConfig {

    @Getter
    private OpenAiStreamClient openAiStreamClient;

    private final IChatConfigService configService;

    private final OpenAiChatModel openAiChatModel;

    @Bean
    @ConditionalOnProperty(value = "audioChat.enable", havingValue = "true", matchIfMissing = false)
    public OpenAiStreamClient openAiStreamClient() {
        String apiHost = configService.getConfigValue("chat", "apiHost");
        String apiKey = configService.getConfigValue("chat", "apiKey");
        openAiStreamClient = createOpenAiStreamClient(apiHost,apiKey);
        return openAiStreamClient;
    }

    public static OpenAiStreamClient createOpenAiStreamClient(String apiHost, String apiKey) {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(600, TimeUnit.SECONDS)
            .readTimeout(600, TimeUnit.SECONDS)
            .build();
        return OpenAiStreamClient.builder()
            .apiHost(apiHost)
            .apiKey(Collections.singletonList(apiKey))
            .keyStrategy(new KeyRandomStrategy())
            .okHttpClient(okHttpClient)
            .build();
    }
}
