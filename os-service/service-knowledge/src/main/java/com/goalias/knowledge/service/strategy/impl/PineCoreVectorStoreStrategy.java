package com.goalias.knowledge.service.strategy.impl;

import com.goalias.common.core.config.VectorStoreProperties;
import com.goalias.common.core.exception.ServiceException;
import com.goalias.knowledge.domain.bo.QueryVectorBo;
import com.goalias.knowledge.domain.bo.StoreEmbeddingBo;
import com.goalias.knowledge.embedding.EmbeddingModelFactory;
import com.goalias.knowledge.service.strategy.AbstractVectorStoreStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class PineCoreVectorStoreStrategy extends AbstractVectorStoreStrategy {

    private final Integer DIMENSION = 2048;

    public PineCoreVectorStoreStrategy(VectorStoreProperties vectorStoreProperties, EmbeddingModelFactory embeddingModelFactory) {
        super(vectorStoreProperties, embeddingModelFactory);
    }

    @Override
    public String getVectorStoreType() {
        return "pinecore";
    }

    @Override
    public void storeEmbeddings(StoreEmbeddingBo storeEmbeddingBo) throws ServiceException {

    }

    @Override
    public List<String> getQueryVector(QueryVectorBo queryVectorBo) {
        return List.of();
    }

    @Override
    public void createSchema(String kid, String embeddingModelName) {

    }

    @Override
    public void removeById(String id, String modelName) throws ServiceException {

    }

    @Override
    public void removeByDocId(String docId, String kid) throws ServiceException {

    }

    @Override
    public void removeByFid(String fid, String kid) throws ServiceException {

    }
}
