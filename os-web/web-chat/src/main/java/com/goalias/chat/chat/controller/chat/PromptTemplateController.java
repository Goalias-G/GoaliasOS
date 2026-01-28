package com.goalias.chat.chat.controller.chat;

import com.goalias.chat.domain.PromptTemplate;
import com.goalias.chat.domain.bo.PromptTemplateBo;
import com.goalias.chat.service.IPromptTemplateService;
import com.goalias.common.core.domain.R;
import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.core.validate.EditGroup;
import com.goalias.common.web.annotation.RepeatSubmit;
import com.goalias.common.web.core.BaseController;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提示词模板
 *
 * @author Goalias
 * @since 2026-01-22 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat/promptTemplate")
public class PromptTemplateController extends BaseController {

    private final IPromptTemplateService promptTemplateService;

    /**
     * 查询提示词模板列表
     */
    @GetMapping("/list")
    public TableDataInfo<PromptTemplate> list(PromptTemplateBo bo, PageQuery pageQuery) {
        return promptTemplateService.queryPageList(bo, pageQuery);
    }


    /**
     * 获取提示词模板详细信息
     *
     * @param id 主键
     */
    @GetMapping("/{id}")
    public R<PromptTemplate> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(promptTemplateService.queryById(id));
    }

    /**
     * 新增提示词模板
     */
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody PromptTemplateBo bo) {
        return toAjax(promptTemplateService.insertByBo(bo));
    }

    /**
     * 修改提示词模板
     */
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody PromptTemplateBo bo) {
        return toAjax(promptTemplateService.updateByBo(bo));
    }

    /**
     * 删除提示词模板
     *
     * @param ids 主键串
     */
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(promptTemplateService.deleteWithValidByIds(List.of(ids), true));
    }
}