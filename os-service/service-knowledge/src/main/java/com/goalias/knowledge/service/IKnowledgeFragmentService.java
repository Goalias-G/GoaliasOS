package com.goalias.knowledge.service;


import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.knowledge.domain.KnowledgeFragment;
import com.goalias.knowledge.domain.bo.KnowledgeFragmentBo;

import java.util.Collection;
import java.util.List;

/**
 * 知识片段Service接口
 *
 * @author Goalias
 * @since 2026-01-16
 */
public interface IKnowledgeFragmentService {

    /**
     * 查询知识片段
     */
    KnowledgeFragment queryById(Long id);

    /**
     * 查询知识片段列表
     */
    TableDataInfo<KnowledgeFragment> queryPageList(KnowledgeFragmentBo bo, PageQuery pageQuery);

    /**
     * 查询知识片段列表
     */
    List<KnowledgeFragment> queryList(KnowledgeFragmentBo bo);

    /**
     * 新增知识片段
     */
    Boolean insertByBo(KnowledgeFragmentBo bo);

    /**
     * 修改知识片段
     */
    Boolean updateByBo(KnowledgeFragmentBo bo);

    /**
     * 校验并批量删除知识片段信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
