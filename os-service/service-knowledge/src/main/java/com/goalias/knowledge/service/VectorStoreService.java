package com.goalias.knowledge.service;

import com.goalias.common.core.exception.ServiceException;
import com.goalias.knowledge.domain.bo.QueryVectorBo;
import com.goalias.knowledge.domain.bo.StoreEmbeddingBo;

import java.util.List;

/**
 * 向量库管理
 * @author Goalias
 */
public interface VectorStoreService {

    void storeEmbeddings(StoreEmbeddingBo storeEmbeddingBo) throws ServiceException;

    List<String> getQueryVector(QueryVectorBo queryVectorBo);

    void createSchema(String kid, String embeddingModelName);

    void removeById(String id,String modelName) throws ServiceException;

    void removeByDocId(String docId, String kid) throws ServiceException;

    void removeByFid(String fid, String kid) throws ServiceException;
}
