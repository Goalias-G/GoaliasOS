package com.goalias.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.goalias.common.core.utils.MapstructUtils;
import com.goalias.system.domain.DailyKnowledge;
import com.goalias.system.domain.bo.DailyKnowledgeBo;
import com.goalias.system.mapper.DailyKnowledgeMapper;
import com.goalias.system.service.IDailyKnowledgeService;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 每日知识Service业务层处理
 *
 * @author Goalias
 */
@RequiredArgsConstructor
@Service
public class DailyKnowledgeServiceImpl extends ServiceImpl<DailyKnowledgeMapper, DailyKnowledge> implements IDailyKnowledgeService {

    @Override
    public DailyKnowledge queryById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public TableDataInfo<DailyKnowledge> queryPageList(DailyKnowledgeBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<DailyKnowledge> lqw = buildQueryWrapper(bo);
        Page<DailyKnowledge> result = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<DailyKnowledge> queryList(DailyKnowledgeBo bo) {
        LambdaQueryWrapper<DailyKnowledge> lqw = buildQueryWrapper(bo);
        lqw.orderByDesc(DailyKnowledge::getCreateTime);
        return baseMapper.selectList(lqw);
    }

    private LambdaQueryWrapper<DailyKnowledge> buildQueryWrapper(DailyKnowledgeBo bo) {
        LambdaQueryWrapper<DailyKnowledge> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getType() != null && !bo.getType().isEmpty(), DailyKnowledge::getType, bo.getType());
        lqw.eq(bo.getKnowledgeDate() != null, DailyKnowledge::getCreateTime, bo.getKnowledgeDate());
        lqw.ge(bo.getStartTime() != null, DailyKnowledge::getCreateTime, bo.getStartTime());
        lqw.le(bo.getEndTime() != null, DailyKnowledge::getCreateTime, bo.getEndTime());
        lqw.like(bo.getContent() != null && !bo.getContent().isEmpty(), DailyKnowledge::getContent, bo.getContent());
        return lqw;
    }

    @Override
    public Boolean insertByBo(DailyKnowledgeBo bo) {
        DailyKnowledge add = MapstructUtils.convert(bo, DailyKnowledge.class);
        validEntityBeforeSave(add);
        return baseMapper.insert(add) > 0;
    }

    @Override
    public Boolean updateByBo(DailyKnowledgeBo bo) {
        DailyKnowledge update = MapstructUtils.convert(bo, DailyKnowledge.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    private void validEntityBeforeSave(DailyKnowledge entity) {
    }

}
