package com.goalias.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.goalias.common.core.exception.ServiceException;
import com.goalias.common.core.utils.MapstructUtils;
import com.goalias.system.domain.FinanceCategory;
import com.goalias.system.domain.FinanceTransaction;
import com.goalias.system.domain.bo.FinanceCategoryBo;
import com.goalias.system.mapper.FinanceCategoryMapper;
import com.goalias.system.mapper.FinanceTransactionMapper;
import com.goalias.system.service.IFinanceCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FinanceCategoryServiceImpl extends ServiceImpl<FinanceCategoryMapper, FinanceCategory> implements IFinanceCategoryService {

    private final FinanceTransactionMapper transactionMapper;

    @Override
    public FinanceCategory queryById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<FinanceCategory> queryList(FinanceCategoryBo bo) {
        LambdaQueryWrapper<FinanceCategory> lqw = buildQueryWrapper(bo);
        return baseMapper.selectList(lqw);
    }

    private LambdaQueryWrapper<FinanceCategory> buildQueryWrapper(FinanceCategoryBo bo) {
        LambdaQueryWrapper<FinanceCategory> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getUserId() != null, FinanceCategory::getUserId, bo.getUserId());
        lqw.eq(bo.getType() != null, FinanceCategory::getType, bo.getType());
        lqw.orderByAsc(FinanceCategory::getId);
        return lqw;
    }

    @Override
    public Long insertByBo(FinanceCategoryBo bo) {
        FinanceCategory add = MapstructUtils.convert(bo, FinanceCategory.class);
        baseMapper.insert(add);
        return add.getId();
    }

    @Override
    public Boolean updateByBo(FinanceCategoryBo bo) {
        FinanceCategory update = MapstructUtils.convert(bo, FinanceCategory.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        Long count = transactionMapper.selectCount(
            new LambdaQueryWrapper<FinanceTransaction>().in(FinanceTransaction::getCategoryId, ids)
        );
        if (count > 0) {
            throw new ServiceException("该分类下存在交易记录，无法删除");
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
