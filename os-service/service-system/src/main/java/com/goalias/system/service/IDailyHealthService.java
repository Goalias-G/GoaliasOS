package com.goalias.system.service;

import com.goalias.system.domain.DailyHealth;
import com.goalias.system.domain.bo.DailyHealthBo;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 每日健康记录Service接口
 *
 * @author Goalias
 */
public interface IDailyHealthService {

    /**
     * 查询每日健康记录
     */
    DailyHealth queryById(Long id);

    /**
     * 查询每日健康记录列表
     */
    TableDataInfo<DailyHealth> queryPageList(DailyHealthBo bo, PageQuery pageQuery);

    /**
     * 查询每日健康记录列表
     */
    List<DailyHealth> queryList(DailyHealthBo bo);

    /**
     * 新增每日健康记录
     */
    Boolean insertByBo(DailyHealthBo bo);

    /**
     * 修改每日健康记录
     */
    Boolean updateByBo(DailyHealthBo bo);

    /**
     * 校验并批量删除每日健康记录
     */
    Boolean deleteWithIds(Collection<Long> ids);

}
