package com.goalias.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.goalias.common.core.utils.MapstructUtils;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.FinanceTransaction;
import com.goalias.system.domain.bo.FinanceTransactionBo;
import com.goalias.system.domain.vo.FinanceTransactionVo;
import com.goalias.system.mapper.FinanceTransactionMapper;
import com.goalias.system.service.IFinanceTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@RequiredArgsConstructor
@Service
public class FinanceTransactionServiceImpl extends ServiceImpl<FinanceTransactionMapper, FinanceTransaction> implements IFinanceTransactionService {

    @Override
    public FinanceTransaction queryById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public TableDataInfo<FinanceTransactionVo> queryPageList(FinanceTransactionBo bo, PageQuery pageQuery) {
        bo.setUserId(LoginHelper.getUserId());
        Page<FinanceTransactionVo> page = baseMapper.selectTransactionPage(
            pageQuery.build(),
            bo.getUserId(),
            bo.getCategoryId(),
            bo.getTag(),
            bo.getStartDate() != null ? bo.getStartDate().toString() : null,
            bo.getEndDate() != null ? bo.getEndDate().plusDays(1).toString() : null
        );
        return TableDataInfo.build(page);
    }

    @Override
    public Boolean insertByBo(FinanceTransactionBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        FinanceTransaction add = MapstructUtils.convert(bo, FinanceTransaction.class);
        return baseMapper.insert(add) > 0;
    }

    @Override
    public Boolean updateByBo(FinanceTransactionBo bo) {
        FinanceTransaction update = MapstructUtils.convert(bo, FinanceTransaction.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithIds(Collection<Long> ids) {
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
