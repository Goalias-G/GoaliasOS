package com.goalias.knowledge.service.strategy.impl;

import com.goalias.common.core.config.VectorStoreProperties;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.knowledge.domain.bo.QueryVectorBo;
import com.goalias.knowledge.domain.bo.StoreEmbeddingBo;
import com.goalias.knowledge.embedding.EmbeddingModelFactory;
import com.goalias.knowledge.service.strategy.AbstractVectorStoreStrategy;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@Slf4j
@Component
public class PineconeVectorStoreStrategy extends AbstractVectorStoreStrategy {

    private final Integer DIMENSION = 1024;

    private final Map<String, EmbeddingStore<TextSegment>> storeCache = new ConcurrentHashMap<>();

    public PineconeVectorStoreStrategy(VectorStoreProperties vectorStoreProperties, EmbeddingModelFactory embeddingModelFactory) {
        super(vectorStoreProperties, embeddingModelFactory);
    }

    // 事先创建索引（nameSpace:  os-userId-kid）
    private EmbeddingStore<TextSegment> getPineconeStore(String nameSpace) {
        return storeCache.computeIfAbsent(nameSpace, k -> PineconeEmbeddingStore.builder()
                .apiKey(vectorStoreProperties.getPinecone().getApiKey())
                .index(vectorStoreProperties.getPinecone().getIndex())//
                .nameSpace(nameSpace)
                .metadataTextKey("text")
                .build());
    }

    @Override
    public String getVectorStoreType() {
        return "pinecone";
    }

    @Override
    public void createSchema(String kid, String modelName) {
        String spaceName = getNameSpace(kid);
        // 使用缓存获取连接以确保只初始化一次
        EmbeddingStore<TextSegment> store = getPineconeStore(spaceName);
        log.info("Pinecone集合初始化完成: {}", spaceName);
    }

    @Override
    public void storeEmbeddings(StoreEmbeddingBo storeEmbeddingBo) {
        EmbeddingModel embeddingModel = getEmbeddingModel(storeEmbeddingBo.getEmbeddingModelName(), DIMENSION);

        List<String> chunkList = storeEmbeddingBo.getChunkList();
        List<String> fidList = storeEmbeddingBo.getFids();
        String kid = storeEmbeddingBo.getKid();
        String docId = storeEmbeddingBo.getDocId();
        String spaceName = getNameSpace(kid);

        log.info("Pinecone向量存储条数记录: {}", chunkList.size());
        long startTime = System.currentTimeMillis();

        EmbeddingStore<TextSegment> embeddingStore = getPineconeStore(spaceName);

        IntStream.range(0, chunkList.size()).forEach(i -> {
            String text = chunkList.get(i);
            String fid = fidList.get(i);
            Metadata metadata = new Metadata();
            metadata.put("kid", kid);
            metadata.put("docId", docId);
            metadata.put("fid", fid);

            TextSegment textSegment = TextSegment.from(text, metadata);
            Embedding embedding = embeddingModel.embed(text).content();
            embeddingStore.add(embedding, textSegment);
        });
        long endTime = System.currentTimeMillis();
        log.info("Pinecone向量存储完成消耗时间：{}秒", (endTime - startTime) / 1000);
    }

    @Override
    public List<String> getQueryVector(QueryVectorBo queryVectorBo) {
        EmbeddingModel embeddingModel = getEmbeddingModel(queryVectorBo.getEmbeddingModelName(), DIMENSION);

        Embedding queryEmbedding = embeddingModel.embed(queryVectorBo.getQuery()).content();
        String spaceName = getNameSpace(queryVectorBo.getKid());

        // 同时是相同kid的向量数据
        EmbeddingStore<TextSegment> embeddingStore = getPineconeStore(spaceName);

        List<String> resultList = new ArrayList<>();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(queryVectorBo.getMaxResults())
//                .minScore(0.3)
                .build();
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(request).matches();
        for (EmbeddingMatch<TextSegment> match : matches) {
            TextSegment segment = match.embedded();
            if (Objects.nonNull(segment)) {
                resultList.add(segment.text());
            }
        }
        return resultList;
    }

    @Override
    @SneakyThrows
    public void removeByKid(String kid) {
        EmbeddingStore<TextSegment> embeddingStore = getPineconeStore(getNameSpace(kid));
        embeddingStore.removeAll();
        log.info("Pinecone成功删除 kid={} 的所有向量数据", kid);
        storeCache.remove(getNameSpace(kid));
//        embeddingStore.remove(kid);
    }

    @Override
    public void removeByDocId(String docId, String kid) {
        EmbeddingStore<TextSegment> embeddingStore = getPineconeStore(getNameSpace(kid));
        Filter filter = MetadataFilterBuilder.metadataKey("docId").isEqualTo(docId);
        embeddingStore.removeAll(filter);
        log.info("Pinecone成功删除 docId={} 的所有向量数据", docId);
    }

    @Override
    public void removeByFid(String fid, String kid) {
        EmbeddingStore<TextSegment> embeddingStore = getPineconeStore(getNameSpace(kid));
        Filter filter = MetadataFilterBuilder.metadataKey("fid").isEqualTo(fid);
        embeddingStore.removeAll(filter);
        log.info("Pinecone成功删除 fid={} 的所有向量数据", fid);
    }

    /**
     * 获取命名空间
     */
    private String getNameSpace(String kid) {
        return "os-" + LoginHelper.getUserId() + "-" + kid;
    }
}
