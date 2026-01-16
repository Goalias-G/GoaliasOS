package com.goalias.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.common.core.exception.ServiceException;
import com.goalias.common.core.utils.StringUtils;
import com.goalias.common.oss.core.MinioService;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.SysOss;
import com.goalias.system.mapper.SysOssMapper;
import com.goalias.system.service.ISysOssService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        return baseMapper.selectByIds(ossIds);
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
        // 生成文件存储路径：userId/年/月/日/文件名
        String objectName = MinioService.getTimeFilePath(LoginHelper.getUserId(), file.getOriginalFilename());

        String originalFilename = file.getOriginalFilename();
        String suffix = StringUtils.substring(originalFilename, originalFilename.lastIndexOf("."),
                originalFilename.length());

        String url = minioService.uploadFile(objectName, file);
        if (StringUtils.isBlank(url)) {
            throw new ServiceException("文件上传失败");
        }
        // 保存文件信息
        SysOss oss = new SysOss();
        oss.setUrl(url);
        oss.setFileSuffix(suffix);
        oss.setFileName(objectName);
        oss.setOriginalName(originalFilename);
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


    @Override
    public Long saveFile(SysOss sysOss) {
        baseMapper.insert(sysOss);
        return sysOss.getOssId();
    }
}
