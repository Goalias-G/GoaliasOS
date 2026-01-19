package com.goalias.knowledge.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.chat.domain.ChatModel;
import com.goalias.chat.enums.ChatModeType;
import com.goalias.chat.service.IChatModelService;
import com.goalias.common.core.domain.model.LoginUser;
import com.goalias.common.core.utils.MapstructUtils;
import com.goalias.common.core.utils.StringUtils;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.knowledge.chain.loader.ResourceLoader;
import com.goalias.knowledge.chain.loader.ResourceLoaderFactory;
import com.goalias.knowledge.domain.KnowledgeAttach;
import com.goalias.knowledge.domain.KnowledgeFragment;
import com.goalias.knowledge.domain.KnowledgeInfo;
import com.goalias.knowledge.domain.bo.KnowledgeInfoBo;
import com.goalias.knowledge.domain.bo.KnowledgeInfoUploadBo;
import com.goalias.knowledge.domain.bo.StoreEmbeddingBo;
import com.goalias.knowledge.domain.vo.KnowledgeInfoVo;
import com.goalias.knowledge.mapper.KnowledgeAttachMapper;
import com.goalias.knowledge.mapper.KnowledgeFragmentMapper;
import com.goalias.knowledge.mapper.KnowledgeInfoMapper;
import com.goalias.knowledge.service.IKnowledgeInfoService;
import com.goalias.knowledge.service.VectorStoreService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * 知识库Service业务层处理
 *
 * @author Goalias
 * @date 2025-04-08
 */
@RequiredArgsConstructor
@Service
public class KnowledgeInfoServiceImpl implements IKnowledgeInfoService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeInfoServiceImpl.class);

    private final KnowledgeInfoMapper baseMapper;

    private final VectorStoreService vectorStoreService;

    private final ResourceLoaderFactory resourceLoaderFactory;

    private final KnowledgeFragmentMapper fragmentMapper;

    private final KnowledgeAttachMapper attachMapper;

    private final IChatModelService chatModelService;

    /**
     * 查询知识库
     */
    @Override
    @Cacheable(cacheNames = CacheNames.KNOWLEDGE_INFO, key = "#id")
    public KnowledgeInfo queryById(Long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 查询知识库列表
     */
    @Override
    public TableDataInfo<KnowledgeInfo> queryPageList(KnowledgeInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<KnowledgeInfo> lqw = buildQueryWrapper(bo);
        Page<KnowledgeInfo> result = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询知识库列表
     */
    @Override
    public List<KnowledgeInfo> queryList(KnowledgeInfoBo bo) {
        LambdaQueryWrapper<KnowledgeInfo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectList(lqw);
    }

    private LambdaQueryWrapper<KnowledgeInfo> buildQueryWrapper(KnowledgeInfoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<KnowledgeInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getKid()), KnowledgeInfo::getKid, bo.getKid());
        lqw.eq(bo.getUid() != null, KnowledgeInfo::getUid, bo.getUid());
        lqw.like(StringUtils.isNotBlank(bo.getKname()), KnowledgeInfo::getKname, bo.getKname());
        lqw.eq(bo.getShare() != null, KnowledgeInfo::getShare, bo.getShare());
        lqw.eq(StringUtils.isNotBlank(bo.getDescription()), KnowledgeInfo::getDescription,
                bo.getDescription());
        lqw.eq(StringUtils.isNotBlank(bo.getKnowledgeSeparator()), KnowledgeInfo::getKnowledgeSeparator,
                bo.getKnowledgeSeparator());
        lqw.eq(StringUtils.isNotBlank(bo.getQuestionSeparator()), KnowledgeInfo::getQuestionSeparator,
                bo.getQuestionSeparator());
        lqw.eq(bo.getOverlapChar() != null, KnowledgeInfo::getOverlapChar, bo.getOverlapChar());
        lqw.eq(bo.getRetrieveLimit() != null, KnowledgeInfo::getRetrieveLimit, bo.getRetrieveLimit());
        lqw.eq(bo.getTextBlockSize() != null, KnowledgeInfo::getTextBlockSize, bo.getTextBlockSize());
        return lqw;
    }

    /**
     * 新增知识库
     */
    @Override
    public Boolean insertByBo(KnowledgeInfoBo bo) {
        KnowledgeInfo add = MapstructUtils.convert(bo, KnowledgeInfo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改知识库
     */
    @Override
    @CachePut(cacheNames = CacheNames.KNOWLEDGE_INFO, key = "#bo.id")
    public Boolean updateByBo(KnowledgeInfoBo bo) {
        KnowledgeInfo update = MapstructUtils.convert(bo, KnowledgeInfo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(KnowledgeInfo entity) {
        // TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除知识库
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            // TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOne(KnowledgeInfoBo bo) {
        KnowledgeInfo knowledgeInfo = MapstructUtils.convert(bo, KnowledgeInfo.class);
        if (StringUtils.isBlank(bo.getKid())) {
            String kid = RandomUtil.randomString(10);
            if (knowledgeInfo != null) {
                knowledgeInfo.setKid(kid);
                knowledgeInfo.setUid(LoginHelper.getLoginUser().getUserId());
            }
            baseMapper.insert(knowledgeInfo);
            if (knowledgeInfo != null) {
                vectorStoreService.createSchema(String.valueOf(knowledgeInfo.getId()), bo.getEmbeddingModelName());
            }
        } else {
            baseMapper.updateById(knowledgeInfo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheNames.KNOWLEDGE_INFO)
    public void removeKnowledge(String kid) {
        Map<String, Object> map = new HashMap<>();
        KnowledgeInfo knowledgeInfo = baseMapper.selectByKid(kid);

        check(knowledgeInfo);
        map.put("kid", knowledgeInfo.getKid());
        // 删除向量数据
        vectorStoreService.removeById(String.valueOf(knowledgeInfo.getId()), knowledgeInfo.getVectorModelName());
        // 删除附件和知识片段
        fragmentMapper.deleteByMap(map);
        attachMapper.deleteByMap(map);
        // 删除知识库
        map.put("kid", knowledgeInfo.getKid());
        baseMapper.deleteByMap(map);
    }

    @Override
    public void upload(KnowledgeInfoUploadBo bo) {
        storeContent(bo.getFile(), bo.getKid());
    }

    public void storeContent(MultipartFile file, String kid) {
        String fileName = file.getOriginalFilename();
        List<String> chunkList = new ArrayList<>();
        KnowledgeAttach knowledgeAttach = new KnowledgeAttach();
        knowledgeAttach.setKid(kid);
        String docId = RandomUtil.randomString(10);
        knowledgeAttach.setDocId(docId);
        knowledgeAttach.setDocName(fileName);
        knowledgeAttach.setDocType(fileName.substring(fileName.lastIndexOf(".") + 1));
        String content = "";
        ResourceLoader resourceLoader = resourceLoaderFactory.getLoaderByFileType(knowledgeAttach.getDocType());
        // 文档分段入库
        List<String> fids = new ArrayList<>();
        try {
            content = resourceLoader.getContent(file.getInputStream());
            chunkList = resourceLoader.getChunkList(content, kid);
            List<KnowledgeFragment> knowledgeFragmentList = new ArrayList<>();
            if (CollUtil.isNotEmpty(chunkList)) {
                for (int i = 0; i < chunkList.size(); i++) {
                    // 生成知识片段ID
                    String fid = RandomUtil.randomString(10);
                    fids.add(fid);
                    KnowledgeFragment knowledgeFragment = new KnowledgeFragment();
                    knowledgeFragment.setKid(kid);
                    knowledgeFragment.setDocId(docId);
                    knowledgeFragment.setFid(fid);
                    knowledgeFragment.setIdx(i);
                    knowledgeFragment.setContent(chunkList.get(i));
                    knowledgeFragment.setCreateTime(new Date());
                    knowledgeFragmentList.add(knowledgeFragment);
                }
            }
            fragmentMapper.insertBatch(knowledgeFragmentList);
        } catch (IOException e) {
            log.error("保存知识库信息失败！{}", e.getMessage());
        }
        knowledgeAttach.setContent(content);
        knowledgeAttach.setCreateTime(new Date());
        attachMapper.insert(knowledgeAttach);

        // 通过kid查询知识库信息
        KnowledgeInfo knowledgeInfoVo = baseMapper.selectOne(Wrappers.<KnowledgeInfo>lambdaQuery()
                .eq(KnowledgeInfo::getId, kid));

        // 通过向量模型查询模型信息
        ChatModel chatModelVo = chatModelService.selectModelByName(knowledgeInfoVo.getEmbeddingModelName());
        // 未查到指定模型时，回退为向量分类最高优先级模型
        if (chatModelVo == null) {
            chatModelVo = chatModelService.selectModelByCategoryWithHighestPriority(ChatModeType.VECTOR.getCode());
        }
        StoreEmbeddingBo storeEmbeddingBo = new StoreEmbeddingBo();
        storeEmbeddingBo.setKid(kid);
        storeEmbeddingBo.setDocId(docId);
        storeEmbeddingBo.setFids(fids);
        storeEmbeddingBo.setChunkList(chunkList);
        storeEmbeddingBo.setVectorStoreName(knowledgeInfoVo.getVectorModelName());
        storeEmbeddingBo.setEmbeddingModelName(knowledgeInfoVo.getEmbeddingModelName());
        storeEmbeddingBo.setApiKey(chatModelVo.getApiKey());
        storeEmbeddingBo.setBaseUrl(chatModelVo.getApiHost());
        vectorStoreService.storeEmbeddings(storeEmbeddingBo);
    }

    /**
     * 检查用户是否有删除知识库权限
     *
     * @param knowledgeInfo 知识库
     */
    public void check(KnowledgeInfo knowledgeInfo) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (!knowledgeInfo.getUid().equals(loginUser.getUserId())) {
            throw new SecurityException("权限不足");
        }
    }

}
