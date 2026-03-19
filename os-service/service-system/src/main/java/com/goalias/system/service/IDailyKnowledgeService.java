package com.goalias.system.service;

import com.goalias.system.domain.DailyKnowledge;
import com.goalias.system.domain.bo.DailyKnowledgeBo;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 每日知识Service接口
 *
 * @author Goalias
 */
public interface IDailyKnowledgeService {

    /**
     * 查询每日知识
     */
    DailyKnowledge queryById(Long id);

    /**
     * 查询每日知识列表
     */
    TableDataInfo<DailyKnowledge> queryPageList(DailyKnowledgeBo bo, PageQuery pageQuery);

    /**
     * 查询每日知识列表
     */
    List<DailyKnowledge> queryList(DailyKnowledgeBo bo);

    /**
     * 新增每日知识
     */
    Boolean insertByBo(DailyKnowledgeBo bo);

    /**
     * 修改每日知识
     */
    Boolean updateByBo(DailyKnowledgeBo bo);

    /**
     * 校验并批量删除每日知识
     */
    Boolean deleteWithIds(Collection<Long> ids);

}
