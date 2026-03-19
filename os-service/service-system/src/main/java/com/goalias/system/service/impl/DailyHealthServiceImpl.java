package com.goalias.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.goalias.common.core.utils.MapstructUtils;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.system.domain.DailyHealth;
import com.goalias.system.domain.bo.DailyHealthBo;
import com.goalias.system.mapper.DailyHealthMapper;
import com.goalias.system.service.IDailyHealthService;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 每日健康记录Service业务层处理
 *
 * @author Goalias
 */
@RequiredArgsConstructor
@Service
public class DailyHealthServiceImpl extends ServiceImpl<DailyHealthMapper, DailyHealth> implements IDailyHealthService {

    @Override
    public DailyHealth queryById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public TableDataInfo<DailyHealth> queryPageList(DailyHealthBo bo, PageQuery pageQuery) {
        bo.setUserId(LoginHelper.getUserId());
        LambdaQueryWrapper<DailyHealth> lqw = buildQueryWrapper(bo);
        Page<DailyHealth> result = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public List<DailyHealth> queryList(DailyHealthBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        LambdaQueryWrapper<DailyHealth> lqw = buildQueryWrapper(bo);
        lqw.orderByDesc(DailyHealth::getCreateTime);
        return baseMapper.selectList(lqw);
    }

    private LambdaQueryWrapper<DailyHealth> buildQueryWrapper(DailyHealthBo bo) {
        LambdaQueryWrapper<DailyHealth> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getUserId() != null, DailyHealth::getUserId, bo.getUserId());
        lqw.eq(bo.getHealthDate() != null, DailyHealth::getCreateTime, bo.getHealthDate());
        lqw.ge(bo.getStartTime() != null, DailyHealth::getCreateTime, bo.getStartTime());
        lqw.le(bo.getEndTime() != null, DailyHealth::getCreateTime, bo.getEndTime());
        return lqw;
    }

    @Override
    public Boolean insertByBo(DailyHealthBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        DailyHealth add = MapstructUtils.convert(bo, DailyHealth.class);
        validEntityBeforeSave(add);
        return baseMapper.insert(add) > 0;
    }

    @Override
    public Boolean updateByBo(DailyHealthBo bo) {
        DailyHealth update = MapstructUtils.convert(bo, DailyHealth.class);
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

    private void validEntityBeforeSave(DailyHealth entity) {
    }

}
