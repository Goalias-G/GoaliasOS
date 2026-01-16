package com.goalias.knowledge.service;


import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.knowledge.domain.bo.KnowledgeInfoBo;
import com.goalias.knowledge.domain.bo.KnowledgeInfoUploadBo;
import com.goalias.knowledge.domain.vo.KnowledgeInfoVo;

import java.util.Collection;
import java.util.List;

/**
 * 知识库Service接口
 *
 * @author Goalias
 * @since 2026-01-16
 */
public interface IKnowledgeInfoService {

    /**
     * 查询知识库
     */
    KnowledgeInfoVo queryById(Long id);

    /**
     * 查询知识库列表
     */
    TableDataInfo<KnowledgeInfoVo> queryPageList(KnowledgeInfoBo bo, PageQuery pageQuery);

    /**
     * 查询知识库列表
     */
    TableDataInfo<KnowledgeInfoVo> queryPageListByRole(KnowledgeInfoBo bo, PageQuery pageQuery);

    /**
     * 查询知识库列表
     */
    List<KnowledgeInfoVo> queryList(KnowledgeInfoBo bo);

    /**
     * 新增知识库
     */
    Boolean insertByBo(KnowledgeInfoBo bo);

    /**
     * 修改知识库
     */
    Boolean updateByBo(KnowledgeInfoBo bo);

    /**
     * 校验并批量删除知识库信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 新增知识库
     */
    void saveOne(KnowledgeInfoBo bo);

    /**
     * 删除知识库
     */
    void removeKnowledge(String kid);

    /**
     * 上传附件
     */
    void upload(KnowledgeInfoUploadBo bo) throws Exception;
}
