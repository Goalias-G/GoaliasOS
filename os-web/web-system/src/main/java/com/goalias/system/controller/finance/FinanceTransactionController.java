package com.goalias.system.controller.finance;

import com.goalias.common.core.domain.R;
import com.goalias.common.web.core.BaseController;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.bo.FinanceTransactionBo;
import com.goalias.system.domain.vo.FinanceTransactionVo;
import com.goalias.system.service.IFinanceTransactionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/finance/transactions")
public class FinanceTransactionController extends BaseController {

    private final IFinanceTransactionService financeTransactionService;

    @GetMapping
    public TableDataInfo<FinanceTransactionVo> list(FinanceTransactionBo bo, PageQuery pageQuery) {
        return financeTransactionService.queryPageList(bo, pageQuery);
    }

    @PostMapping
    public R<Void> add(@Validated @RequestBody FinanceTransactionBo bo) {
        return toAjax(financeTransactionService.insertByBo(bo));
    }

    @PutMapping("/{id}")
    public R<Void> edit(@PathVariable Long id, @Validated @RequestBody FinanceTransactionBo bo) {
        bo.setId(id);
        return toAjax(financeTransactionService.updateByBo(bo));
    }

    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        return toAjax(financeTransactionService.deleteWithIds(List.of(id)));
    }
}
