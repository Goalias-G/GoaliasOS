package com.goalias.chat.service;

import com.goalias.chat.domain.PromptTemplate;
import com.goalias.chat.domain.bo.PromptTemplateBo;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 提示词模板Service接口
 *
 * @author Goalias
 * @since 2026-01-16
 */
public interface IPromptTemplateService {

    /**
     * 查询提示词模板
     */
    PromptTemplate queryById(Long id);

    /**
     * 查询提示词模板列表
     */
    TableDataInfo<PromptTemplate> queryPageList(PromptTemplateBo bo, PageQuery pageQuery);

    /**
     * 查询提示词模板列表
     */
    List<PromptTemplate> queryList(PromptTemplateBo bo);

    /**
     * 新增提示词模板
     */
    Boolean insertByBo(PromptTemplateBo bo);

    /**
     * 修改提示词模板
     */
    Boolean updateByBo(PromptTemplateBo bo);

    /**
     * 校验并批量删除提示词模板信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 根据分类查询提示词模板
     *
     * @param category 分类
     */
    PromptTemplate queryByCategory(String category);
}
