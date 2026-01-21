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

    /**
     * 根据 kid 删除向量数据
     */
    void removeByKid(String kid, String modelName) throws ServiceException;

    /**
     * 根据上传所属文件 删除相关向量数据(docId)
     */
    void removeByDocId(String docId, String kid) throws ServiceException;

    /**
     * 根据 chunk id 删除向量数据（fid）
     */
    void removeByFid(String fid, String kid) throws ServiceException;
}
