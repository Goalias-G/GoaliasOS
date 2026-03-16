package com.goalias.system.service;

import com.goalias.system.domain.LifeCategory;
import com.goalias.system.domain.bo.LifeCategoryBo;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 生活分类Service接口
 *
 * @author Goalias
 */
public interface ILifeCategoryService {

    /**
     * 查询生活分类
     */
    LifeCategory queryById(Long id);

    /**
     * 查询生活分类列表
     */
    TableDataInfo<LifeCategory> queryPageList(LifeCategoryBo bo, PageQuery pageQuery);

    /**
     * 查询生活分类列表
     */
    List<LifeCategory> queryList(LifeCategoryBo bo);

    /**
     * 新增生活分类
     */
    Long insertByBo(LifeCategoryBo bo);

    /**
     * 修改生活分类
     */
    Boolean updateByBo(LifeCategoryBo bo);

    /**
     * 更新排序
     */
    Boolean updateOrder(LifeCategoryBo bo);

    /**
     * 校验并批量删除生活分类
     */
    Boolean deleteWithValidByIds(Collection<Long> ids);

}
