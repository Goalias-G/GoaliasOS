package com.goalias.system.service;

import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.FinanceTransaction;
import com.goalias.system.domain.bo.FinanceTransactionBo;
import com.goalias.system.domain.vo.FinanceTransactionVo;

import java.util.Collection;

public interface IFinanceTransactionService {

    FinanceTransaction queryById(Long id);

    TableDataInfo<FinanceTransactionVo> queryPageList(FinanceTransactionBo bo, PageQuery pageQuery);

    Boolean insertByBo(FinanceTransactionBo bo);

    Boolean updateByBo(FinanceTransactionBo bo);

    Boolean deleteWithIds(Collection<Long> ids);
}
