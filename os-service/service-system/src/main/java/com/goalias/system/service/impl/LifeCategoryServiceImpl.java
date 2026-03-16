package com.goalias.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.goalias.common.core.utils.MapstructUtils;
import com.goalias.common.core.utils.StringUtils;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.system.domain.LifeCategory;
import com.goalias.system.domain.LifeRecord;
import com.goalias.system.domain.bo.LifeCategoryBo;
import com.goalias.system.domain.vo.LifeRecordCountVo;
import com.goalias.system.mapper.LifeCategoryMapper;
import com.goalias.system.mapper.LifeRecordMapper;
import com.goalias.system.service.ILifeCategoryService;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.service.ILifeRecordService;
import com.goalias.system.service.ISysOssService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 生活分类Service业务层处理
 *
 * @author Goalias
 */
@RequiredArgsConstructor
@Service
public class LifeCategoryServiceImpl extends ServiceImpl<LifeCategoryMapper, LifeCategory> implements ILifeCategoryService {

    private final LifeRecordMapper recordMapper;

    private final ILifeRecordService recordService;

    private final ISysOssService ossService;

    @Override
    public LifeCategory queryById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public TableDataInfo<LifeCategory> queryPageList(LifeCategoryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<LifeCategory> lqw = buildQueryWrapper(bo);
        Page<LifeCategory> result = baseMapper.selectPage(pageQuery.build(), lqw);
        if (Objects.nonNull(result) && CollUtil.isNotEmpty(result.getRecords())) {
            List<Long> list = result.getRecords().stream().map(LifeCategory::getId).toList();
            List<LifeRecordCountVo> lifeRecordCountVos = recordMapper.queryRecordCountByCategoryId(list);
            Map<Long, Integer> recordCountMap = lifeRecordCountVos.stream().collect(Collectors.toMap(LifeRecordCountVo::getCategoryId, LifeRecordCountVo::getCount));
            result.getRecords().forEach(record -> record.setRecordCount(recordCountMap.getOrDefault(record.getId(), 0)));
        }
        return TableDataInfo.build(result);
    }

    @Override
    public List<LifeCategory> queryList(LifeCategoryBo bo) {
        LambdaQueryWrapper<LifeCategory> lqw = buildQueryWrapper(bo);
        return baseMapper.selectList(lqw);
    }

    private LambdaQueryWrapper<LifeCategory> buildQueryWrapper(LifeCategoryBo bo) {
        LambdaQueryWrapper<LifeCategory> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getUserId() != null, LifeCategory::getUserId, bo.getUserId());
        lqw.eq(StringUtils.isNotBlank(bo.getName()), LifeCategory::getName, bo.getName());
        lqw.orderByAsc(LifeCategory::getSortOrder);
        return lqw;
    }

    @Override
    public Long insertByBo(LifeCategoryBo bo) {
        LifeCategory add = MapstructUtils.convert(bo, LifeCategory.class);
        if (add.getSortOrder() == null) {
            LambdaQueryWrapper<LifeCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LifeCategory::getUserId, LoginHelper.getUserId());
            wrapper.orderByDesc(LifeCategory::getSortOrder);
            wrapper.last("LIMIT 1");
            LifeCategory lastCategory = baseMapper.selectOne(wrapper);
            add.setSortOrder(lastCategory == null || lastCategory.getSortOrder() == null ? 1 : lastCategory.getSortOrder() + 1);
        }
        validEntityBeforeSave(add);
        baseMapper.insert(add);
        return add.getId();
    }

    @Override
    public Boolean updateByBo(LifeCategoryBo bo) {
        LifeCategory update = MapstructUtils.convert(bo, LifeCategory.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean updateOrder(LifeCategoryBo bo) {
        LifeCategory update = new LifeCategory();
        update.setId(bo.getId());
        update.setSortOrder(bo.getSortOrder());
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        boolean b = baseMapper.deleteBatchIds(ids) > 0;
        if (b) {
            List<LifeRecord> lifeRecords = recordMapper.selectList(new LambdaQueryWrapper<LifeRecord>().select(LifeRecord::getId, LifeRecord::getAttachsId).in(LifeRecord::getCategoryId, ids));
            List<Long> ossIdList = lifeRecords.stream()
                    .map(LifeRecord::getAttachsId)
                    .filter(Objects::nonNull)
                    .flatMap(s -> Arrays.stream(s.split(",")))
                    .map(Long::parseLong)
                    .toList();
            recordService.deleteWithAttachIds(lifeRecords.stream().map(LifeRecord::getId).toList(), ossIdList);
        }
        return b;
    }

    private void validEntityBeforeSave(LifeCategory entity) {
    }

}
