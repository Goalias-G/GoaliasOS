package com.goalias.system.controller.life;

import com.goalias.common.core.domain.R;
import com.goalias.common.core.exception.ServiceException;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.common.web.core.BaseController;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.LifeRecord;
import com.goalias.system.domain.bo.LifeRecordBo;
import com.goalias.system.domain.bo.LifeRecordDeleteBo;
import com.goalias.system.service.ILifeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Objects;

/**
 * 生活记录Controller
 *
 * @author Goalias
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/life/record")
public class LifeRecordController extends BaseController {

    private final ILifeRecordService lifeRecordService;

    /**
     * 查询生活记录列表
     */
    @GetMapping("/list")
    public TableDataInfo<LifeRecord> list(@Validated LifeRecordBo bo, PageQuery pageQuery) {
        if (Objects.isNull(bo.getCategoryId())){
            throw new ServiceException("分类id不可为空!");
        }
        return lifeRecordService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取生活记录详细信息
     *
     * @param id 主键
     */
    @GetMapping(value = "/{id}")
    public R<LifeRecord> getInfo(@PathVariable Long id) {
        return R.ok(lifeRecordService.queryById(id));
    }

    /**
     * 新增生活记录
     */
    @PostMapping
    public R<Void> add(@Validated @RequestBody LifeRecordBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        return toAjax(lifeRecordService.insertByBo(bo));
    }

    /**
     * 修改生活记录
     */
    @PutMapping
    public R<Void> edit(@Validated @RequestBody LifeRecordBo bo) {
        return toAjax(lifeRecordService.updateByBo(bo));
    }

    /**
     * 更新收藏状态
     */
    @PutMapping("/favorite")
    public R<Void> updateFavorite(@RequestBody LifeRecordBo bo) {
        return toAjax(lifeRecordService.updateFavorite(bo.getId(), bo.getFavoriteFlag()));
    }

    /**
     * 更新打分状态
     */
    @PutMapping("/rating")
    public R<Void> updateRating(@RequestBody LifeRecordBo bo) {
        return toAjax(lifeRecordService.updateRating(bo.getId(), bo.getRating()));
    }



    /**
     * 删除生活记录
     */
    @DeleteMapping
    public R<Void> remove(@RequestBody LifeRecordDeleteBo deleteBo) {
        return toAjax(lifeRecordService.deleteWithAttachIds(deleteBo.getIds(), deleteBo.getFileIds()));
    }

}
