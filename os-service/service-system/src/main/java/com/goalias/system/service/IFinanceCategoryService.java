package com.goalias.system.service;

import com.goalias.system.domain.FinanceCategory;
import com.goalias.system.domain.bo.FinanceCategoryBo;

import java.util.Collection;
import java.util.List;

public interface IFinanceCategoryService {

    FinanceCategory queryById(Long id);

    List<FinanceCategory> queryList(FinanceCategoryBo bo);

    Long insertByBo(FinanceCategoryBo bo);

    Boolean updateByBo(FinanceCategoryBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids);
}
