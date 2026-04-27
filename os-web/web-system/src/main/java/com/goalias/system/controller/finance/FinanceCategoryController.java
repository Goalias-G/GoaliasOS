package com.goalias.system.controller.finance;

import com.goalias.common.core.domain.R;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.common.web.core.BaseController;
import com.goalias.system.domain.FinanceCategory;
import com.goalias.system.domain.bo.FinanceCategoryBo;
import com.goalias.system.service.IFinanceCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/finance/categories")
public class FinanceCategoryController extends BaseController {

    private final IFinanceCategoryService financeCategoryService;

    @GetMapping
    public R<List<FinanceCategory>> list(FinanceCategoryBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        return R.ok(financeCategoryService.queryList(bo));
    }

    @PostMapping
    public R<Void> add(@Validated @RequestBody FinanceCategoryBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        return toAjax(financeCategoryService.insertByBo(bo) > 0);
    }

    @PutMapping("/{id}")
    public R<Void> edit(@PathVariable Long id, @Validated @RequestBody FinanceCategoryBo bo) {
        bo.setId(id);
        return toAjax(financeCategoryService.updateByBo(bo));
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        return toAjax(financeCategoryService.deleteWithValidByIds(List.of(id)));
    }
}
