package com.goalias.system.controller.life;

import com.goalias.common.core.domain.R;
import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.common.web.core.BaseController;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.LifeCategory;
import com.goalias.system.domain.bo.LifeCategoryBo;
import com.goalias.system.service.ILifeCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 生活分类Controller
 *
 * @author Goalias
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/life/category")
public class LifeCategoryController extends BaseController {

    private final ILifeCategoryService lifeCategoryService;

    /**
     * 查询生活分类列表
     */
    @GetMapping("/list")
    public TableDataInfo<LifeCategory> list(LifeCategoryBo bo, PageQuery pageQuery) {
        bo.setUserId(LoginHelper.getUserId());
        return lifeCategoryService.queryPageList(bo, pageQuery);
    }


    /**
     * 新增生活分类
     */
    @PostMapping
    public R<Void> add(@Validated @RequestBody LifeCategoryBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        return toAjax(lifeCategoryService.insertByBo(bo) > 0);
    }

    /**
     * 修改生活分类
     */
    @PutMapping
    public R<Void> edit(@Validated @RequestBody LifeCategoryBo bo) {
        return toAjax(lifeCategoryService.updateByBo(bo));
    }

    /**
     * 更新排序
     */
    @PutMapping("/order")
    public R<Void> updateOrder(@Validated @RequestBody LifeCategoryBo bo) {
        return toAjax(lifeCategoryService.updateOrder(bo));
    }

    /**
     * 删除生活分类
     *
     * @param ids 主键串
     */
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return toAjax(lifeCategoryService.deleteWithValidByIds(Arrays.asList(ids)));
    }

}
