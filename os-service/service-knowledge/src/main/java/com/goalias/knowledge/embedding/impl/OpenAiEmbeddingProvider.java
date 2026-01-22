package com.goalias.knowledge.embedding.impl;

import com.goalias.chat.domain.ChatModel;
import com.goalias.knowledge.embedding.BaseEmbedModelService;
import com.goalias.knowledge.embedding.model.ModalityType;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * @author Goalias
 * @since 2026-01-22 * @apiNote: OpenAi嵌入模型
 */
@Component("openai")
public class OpenAiEmbeddingProvider implements BaseEmbedModelService {
    protected ChatModel chatModel;

    @Override
    public void configure(ChatModel config) {
        this.chatModel = config;
    }

    @Override
    public Set<ModalityType> getSupportedModalities() {
        return Set.of(ModalityType.TEXT);
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(chatModel.getApiHost())
                .apiKey(chatModel.getApiKey())
                .modelName(chatModel.getModelName())
                .dimensions(chatModel.getDimension())
                .build()
                .embedAll(textSegments);
    }
}
