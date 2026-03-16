package com.goalias.system.service;

import com.goalias.system.domain.LifeRecord;
import com.goalias.system.domain.bo.LifeRecordBo;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 生活记录Service接口
 *
 * @author Goalias
 */
public interface ILifeRecordService {

    /**
     * 查询生活记录
     */
    LifeRecord queryById(Long id);

    /**
     * 查询生活记录列表
     */
    TableDataInfo<LifeRecord> queryPageList(LifeRecordBo bo, PageQuery pageQuery);

    /**
     * 查询生活记录列表
     */
    List<LifeRecord> queryList(LifeRecordBo bo);

    /**
     * 新增生活记录
     */
    Boolean insertByBo(LifeRecordBo bo);

    /**
     * 修改生活记录
     */
    Boolean updateByBo(LifeRecordBo bo);

    /**
     * 更新收藏状态
     */
    Boolean updateFavorite(Long id, Integer favoriteFlag);

    Boolean updateRating(Long id, Integer rating);

    /**
     * 校验并批量删除生活记录
     */
    Boolean deleteWithAttachIds(Collection<Long> ids, List<Long> attachIds);

}
