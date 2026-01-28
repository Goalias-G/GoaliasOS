package com.goalias.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.chat.domain.PromptTemplate;
import com.goalias.chat.domain.bo.PromptTemplateBo;
import com.goalias.chat.mapper.PromptTemplateMapper;
import com.goalias.chat.service.IPromptTemplateService;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import lombok.RequiredArgsConstructor;
import com.goalias.common.core.utils.MapstructUtils;
import com.goalias.common.core.utils.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 提示词模板Service业务层处理
 *
 * @author Goalias
 * @since 2026-01-22 */
@Service
@RequiredArgsConstructor
public class PromptTemplateServiceImpl implements IPromptTemplateService {

    private final PromptTemplateMapper baseMapper;

    /**
     * 查询提示词模板
     */
    @Override
    public PromptTemplate queryById(Long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 查询提示词模板列表
     */
    @Override
    public TableDataInfo<PromptTemplate> queryPageList(PromptTemplateBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<PromptTemplate> lqw = buildQueryWrapper(bo);
        Page<PromptTemplate> result = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询提示词模板列表
     */
    @Override
    public List<PromptTemplate> queryList(PromptTemplateBo bo) {
        LambdaQueryWrapper<PromptTemplate> lqw = buildQueryWrapper(bo);
        return baseMapper.selectList(lqw);
    }

    private LambdaQueryWrapper<PromptTemplate> buildQueryWrapper(PromptTemplateBo bo) {
        LambdaQueryWrapper<PromptTemplate> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getTemplateName()),
                PromptTemplate::getTemplateName, bo.getTemplateName());
        lqw.like(StringUtils.isNotBlank(bo.getTemplateContent()),
                PromptTemplate::getTemplateContent, bo.getTemplateContent());
        lqw.eq(StringUtils.isNotBlank(bo.getCategory()),
                PromptTemplate::getCategory, bo.getCategory());
        return lqw;
    }

    /**
     * 新增提示词模板
     */
    @Override
    @CacheEvict(cacheNames = CacheNames.CHAT_PROMPT, key = "#bo.category")
    public Boolean insertByBo(PromptTemplateBo bo) {
        PromptTemplate add = MapstructUtils.convert(bo, PromptTemplate.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改提示词模板
     */
    @Override
    @CacheEvict(cacheNames = CacheNames.CHAT_PROMPT, key = "#bo.category")
    public Boolean updateByBo(PromptTemplateBo bo) {
        PromptTemplate update = MapstructUtils.convert(bo, PromptTemplate.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(PromptTemplate entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除提示词模板
     */
    @Override
    @CacheEvict(cacheNames = CacheNames.CHAT_PROMPT)
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    @Cacheable(cacheNames = CacheNames.CHAT_PROMPT, key = "#category", unless = "#result == null")
    public PromptTemplate queryByCategory(String category) {
        LambdaQueryWrapper<PromptTemplate> queryWrapper = Wrappers.lambdaQuery(PromptTemplate.class);
        queryWrapper.eq(PromptTemplate::getCategory, category);
        queryWrapper.orderByDesc(PromptTemplate::getPriority);
        return baseMapper.selectOne(queryWrapper, false);
    }
}