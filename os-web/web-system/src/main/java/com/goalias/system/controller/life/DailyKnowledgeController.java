package com.goalias.system.controller.life;

import com.goalias.common.core.domain.R;
import com.goalias.common.web.core.BaseController;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.DailyKnowledge;
import com.goalias.system.domain.bo.DailyKnowledgeBo;
import com.goalias.system.service.IDailyKnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 每日知识Controller
 *
 * @author Goalias
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/life/knowledge")
public class DailyKnowledgeController extends BaseController {

    private final IDailyKnowledgeService dailyKnowledgeService;

    /**
     * 查询每日知识列表
     */
    @GetMapping("/list")
    public TableDataInfo<DailyKnowledge> list(@Validated DailyKnowledgeBo bo, PageQuery pageQuery) {
        return dailyKnowledgeService.queryPageList(bo, pageQuery);
    }

    /**
     * 删除每日知识
     */
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        return toAjax(dailyKnowledgeService.deleteWithIds(ids));
    }

}
