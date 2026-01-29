package com.goalias.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.common.core.utils.MapstructUtils;
import com.goalias.common.core.utils.StringUtils;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.service.ISysOssService;
import lombok.RequiredArgsConstructor;
import com.goalias.knowledge.domain.KnowledgeAttach;
import com.goalias.knowledge.domain.bo.KnowledgeAttachBo;
import com.goalias.knowledge.mapper.KnowledgeAttachMapper;
import com.goalias.knowledge.mapper.KnowledgeFragmentMapper;
import com.goalias.knowledge.service.IKnowledgeAttachService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * 知识库附件Service业务层处理
 *
 * @author Goalias
 * @since 2026-01-22
 */
@RequiredArgsConstructor
@Service
public class KnowledgeAttachServiceImpl implements IKnowledgeAttachService {

    private final KnowledgeAttachMapper baseMapper;
    private final KnowledgeFragmentMapper fragmentMapper;
    private final ISysOssService ossService;


    /**
     * 查询知识库附件
     */
    @Override
    public KnowledgeAttach queryById(Long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 查询知识库附件列表
     */
    public TableDataInfo<KnowledgeAttach> queryPageList(KnowledgeAttachBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<KnowledgeAttach> lqw = buildQueryWrapper(bo);
        Page<KnowledgeAttach> result = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询知识库附件列表
     */
    @Override
    public List<KnowledgeAttach> queryList(KnowledgeAttachBo bo) {
        LambdaQueryWrapper<KnowledgeAttach> lqw = buildQueryWrapper(bo);
        return baseMapper.selectList(lqw);
    }

    private LambdaQueryWrapper<KnowledgeAttach> buildQueryWrapper(KnowledgeAttachBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<KnowledgeAttach> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getKid()), KnowledgeAttach::getKid, bo.getKid());
        lqw.eq(StringUtils.isNotBlank(bo.getDocId()), KnowledgeAttach::getDocId, bo.getDocId());
        lqw.like(StringUtils.isNotBlank(bo.getDocName()), KnowledgeAttach::getDocName, bo.getDocName());
        lqw.eq(StringUtils.isNotBlank(bo.getDocType()), KnowledgeAttach::getDocType, bo.getDocType());
        lqw.eq(StringUtils.isNotBlank(bo.getContent()), KnowledgeAttach::getContent, bo.getContent());
        return lqw;
    }

    /**
     * 新增知识库附件
     */
    @Override
    public Boolean insertByBo(KnowledgeAttachBo bo) {
        KnowledgeAttach add = MapstructUtils.convert(bo, KnowledgeAttach.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改知识库附件
     */
    @Override
    public Boolean updateByBo(KnowledgeAttachBo bo) {
        KnowledgeAttach update = MapstructUtils.convert(bo, KnowledgeAttach.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(KnowledgeAttach entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除知识库附件
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public void removeKnowledgeAttach(String docId) {
        Map<String, Object> map = new HashMap<>();
        map.put("doc_id", docId);
        KnowledgeAttach knowledgeAttach = baseMapper.selectOne(new LambdaQueryWrapper<KnowledgeAttach>().eq(KnowledgeAttach::getDocId, docId));
        if (Objects.nonNull(knowledgeAttach)) {
            ossService.deleteWithValidByIds(Collections.singletonList(knowledgeAttach.getOssId()), true);
            baseMapper.deleteByMap(map);
            fragmentMapper.deleteByMap(map);
        }
    }

    @Override
    public String translationByFile(MultipartFile file, String targetLanguage) {
        return "接口开发中!";
    }
}
