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
import com.goalias.system.domain.SysOss;
import com.goalias.system.domain.bo.LifeRecordBo;
import com.goalias.system.mapper.LifeCategoryMapper;
import com.goalias.system.mapper.LifeRecordMapper;
import com.goalias.system.service.ILifeRecordService;
import com.goalias.system.service.ISysOssService;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 生活记录Service业务层处理
 *
 * @author Goalias
 */
@RequiredArgsConstructor
@Service
public class LifeRecordServiceImpl extends ServiceImpl<LifeRecordMapper, LifeRecord> implements ILifeRecordService {

    private final ISysOssService ossService;

    @Override
    public LifeRecord queryById(Long id) {
        LifeRecord record = baseMapper.selectById(id);
        if (record != null) {
            fillAttachsUrls(record);
        }
        return record;
    }

    @Override
    public TableDataInfo<LifeRecord> queryPageList(LifeRecordBo bo, PageQuery pageQuery) {
        bo.setUserId(LoginHelper.getUserId());
        LambdaQueryWrapper<LifeRecord> lqw = buildQueryWrapper(bo);

        lqw.eq(LifeRecord::getUserId, bo.getUserId());
        lqw.eq(LifeRecord::getCategoryId, bo.getCategoryId());
        lqw.eq(bo.getFavoriteFlag() != null, LifeRecord::getFavoriteFlag, bo.getFavoriteFlag());
        lqw.orderByDesc(LifeRecord::getRecordDate);
        lqw.orderByDesc(LifeRecord::getCreateTime);
        Page<LifeRecord> result = baseMapper.selectPage(pageQuery.build(), lqw);
//        List<LifeRecord> records = result.getRecords();
//        for (LifeRecord record : records) {
//            fillAttachsUrls(record);
//        }
        return TableDataInfo.build(result);
    }

    @Override
    public List<LifeRecord> queryList(LifeRecordBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        LambdaQueryWrapper<LifeRecord> lqw = buildQueryWrapper(bo);
        List<LifeRecord> records = baseMapper.selectList(lqw);
        for (LifeRecord record : records) {
            fillAttachsUrls(record);
        }
        return records;
    }

    private LambdaQueryWrapper<LifeRecord> buildQueryWrapper(LifeRecordBo bo) {
        LambdaQueryWrapper<LifeRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getUserId() != null, LifeRecord::getUserId, bo.getUserId());
        return lqw;
    }

    @Override
    public Boolean insertByBo(LifeRecordBo bo) {
        LifeRecord add = MapstructUtils.convert(bo, LifeRecord.class);
        validEntityBeforeSave(add);
        return baseMapper.insert(add) > 0;
    }

    @Override
    public Boolean updateByBo(LifeRecordBo bo) {
        LifeRecord update = MapstructUtils.convert(bo, LifeRecord.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean updateFavorite(Long id, Integer favoriteFlag) {
        LifeRecord update = new LifeRecord();
        update.setId(id);
        update.setFavoriteFlag(favoriteFlag);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean updateRating(Long id, Integer rating) {
        LifeRecord update = new LifeRecord();
        update.setId(id);
        update.setRating(rating);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithAttachIds(Collection<Long> ids, List<Long> attachIds) {
        boolean b = baseMapper.deleteBatchIds(ids) > 0;
        if (b) {
            if (CollUtil.isNotEmpty(attachIds)) {
                ossService.deleteWithValidByIds(attachIds, true);
            }
        }
        return b;
    }

    private void validEntityBeforeSave(LifeRecord entity) {
    }

    private void fillAttachsUrls(LifeRecord record) {
        if (StringUtils.isNotBlank(record.getAttachsId())) {
            List<String> urls = new ArrayList<>();
            String[] ossIds = record.getAttachsId().split(",");
            List<SysOss> ossList = ossService.listByIds(Arrays.stream(ossIds).map(Long::parseLong).collect(Collectors.toList()));
            if (CollUtil.isNotEmpty(ossList)) {
                for (SysOss oss : ossList) {
                    urls.add(oss.getUrl());
                }
            }
            record.setAttachsUrls(urls);
        }
    }

}
