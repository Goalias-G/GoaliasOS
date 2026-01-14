package com.goalias.system.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.common.core.exception.ServiceException;
import com.goalias.common.core.utils.SpringUtils;
import com.goalias.common.core.utils.StringUtils;
import com.goalias.common.oss.core.MinioService;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.SysOss;
import com.goalias.system.mapper.SysOssMapper;
import com.goalias.system.service.ISysOssService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 文件上传 服务层实现
 *
 * @author Goalias
 */
@RequiredArgsConstructor
@Service
public class SysOssServiceImpl implements ISysOssService {

    private final SysOssMapper baseMapper;
    private final MinioService minioService;

    @Override
    public TableDataInfo<SysOss> queryPageList(SysOss bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysOss> lqw = buildQueryWrapper(bo);
        Page<SysOss> result = baseMapper.selectPage(pageQuery.build(), lqw);
        result.setRecords(result.getRecords());
        return TableDataInfo.build(result);
    }

    @Override
    public List<SysOss> listByIds(Collection<Long> ossIds) {
        List<SysOss> list = new ArrayList<>();
        for (Long id : ossIds) {
            SysOss sysOss = SpringUtils.getAopProxy(this).getById(id);
            if (ObjectUtil.isNotNull(sysOss)) {
                list.add(sysOss);
            }
        }
        return list;
    }

    @Override
    public String selectUrlByIds(String ossIds) {
        List<String> list = new ArrayList<>();
        for (Long id : StringUtils.splitTo(ossIds, Convert::toLong)) {
            SysOss vo = SpringUtils.getAopProxy(this).getById(id);
            if (ObjectUtil.isNotNull(vo)) {
                list.add(vo.getUrl());
            }
        }
        return String.join(StringUtils.SEPARATOR, list);
    }

    private LambdaQueryWrapper<SysOss> buildQueryWrapper(SysOss bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysOss> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getFileName()), SysOss::getFileName, bo.getFileName());
        lqw.like(StringUtils.isNotBlank(bo.getOriginalName()), SysOss::getOriginalName,
                bo.getOriginalName());
        lqw.eq(StringUtils.isNotBlank(bo.getFileSuffix()), SysOss::getFileSuffix, bo.getFileSuffix());
        lqw.eq(StringUtils.isNotBlank(bo.getUrl()), SysOss::getUrl, bo.getUrl());
        lqw.between(params.get("beginCreateTime") != null && params.get("endCreateTime") != null,
                SysOss::getCreateTime, params.get("beginCreateTime"), params.get("endCreateTime"));
        lqw.eq(ObjectUtil.isNotNull(bo.getCreateBy()), SysOss::getCreateBy, bo.getCreateBy());
        return lqw;
    }

    @Cacheable(cacheNames = CacheNames.SYS_OSS, key = "#ossId")
    @Override
    public SysOss getById(Long ossId) {
        return baseMapper.selectById(ossId);
    }

    @Override
    public SysOss upload(MultipartFile file) {
        String originalfileName = file.getOriginalFilename();
        String suffix = StringUtils.substring(originalfileName, originalfileName.lastIndexOf("."),
                originalfileName.length());

        String url;
        url = minioService.uploadFile(originalfileName, file);//TODO 根据业务划分文件
        // 保存文件信息
        SysOss oss = new SysOss();
        oss.setUrl(url);
        oss.setFileSuffix(suffix);
        oss.setFileName(originalfileName);
        oss.setOriginalName(originalfileName);
        baseMapper.insert(oss);
        return oss;
    }

    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            // 做一些业务上的校验,判断是否需要校验
        }
        List<SysOss> list = baseMapper.selectList(new LambdaQueryWrapper<SysOss>().in(SysOss::getOssId, ids));
        minioService.delFile(list.stream().map(SysOss::getFileName).toList());
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 根据文件路径删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    @Override
    public boolean deleteFile(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return false;
        }

        try {
            java.io.File file = new java.io.File(filePath);
            if (file.exists() && file.isFile()) {
                return file.delete();
            }
            return false;
        } catch (Exception e) {
            throw new ServiceException("删除文件失败: " + e.getMessage());
        }
    }
}
