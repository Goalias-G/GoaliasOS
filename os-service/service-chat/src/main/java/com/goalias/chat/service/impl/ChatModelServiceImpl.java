package com.goalias.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.chat.domain.ChatModel;
import com.goalias.chat.domain.bo.ChatModelBo;
import com.goalias.chat.domain.vo.ChatModelVo;
import com.goalias.chat.mapper.ChatModelMapper;
import com.goalias.chat.service.IChatModelService;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import lombok.RequiredArgsConstructor;
import com.goalias.common.core.utils.MapstructUtils;
import com.goalias.common.core.utils.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 聊天模型Service业务层处理
 *
 * @author Goalias
 * @since 2026-01-22
 */
@RequiredArgsConstructor
@Service
public class ChatModelServiceImpl implements IChatModelService {

    private final ChatModelMapper baseMapper;


    /**
     * 查询聊天模型
     */
    @Override
    public ChatModelVo queryById(Long id) {
        return MapstructUtils.convert(baseMapper.selectById(id), ChatModelVo.class);
    }

    /**
     * 查询聊天模型列表
     */
    @Override
    public TableDataInfo<ChatModel> queryPageList(ChatModelBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ChatModel> lqw = buildQueryWrapper(bo);
        lqw.orderByDesc(ChatModel::getCreateTime);
        Page<ChatModel> result = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询聊天模型列表
     */
    @Override
    public List<ChatModelVo> queryList(ChatModelBo bo) {
        LambdaQueryWrapper<ChatModel> lqw = buildQueryWrapper(bo);
        return MapstructUtils.convert(baseMapper.selectList(lqw), ChatModelVo.class);
    }

    private LambdaQueryWrapper<ChatModel> buildQueryWrapper(ChatModelBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ChatModel> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getCategory()), ChatModel::getCategory, bo.getCategory());
        lqw.like(StringUtils.isNotBlank(bo.getModelName()), ChatModel::getModelName, bo.getModelName());
        lqw.eq(StringUtils.isNotBlank(bo.getModelType()), ChatModel::getModelType, bo.getModelType());
        lqw.eq(StringUtils.isNotBlank(bo.getModelShow()), ChatModel::getModelShow, bo.getModelShow());
        return lqw;
    }

    /**
     * 新增聊天模型
     */
    @Override
    public Boolean insertByBo(ChatModelBo bo) {
        ChatModel add = MapstructUtils.convert(bo, ChatModel.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改聊天模型
     */
    @Override
    @CacheEvict(value = CacheNames.CHAT_MODEL, key = "#bo.modelName")
    public Boolean updateByBo(ChatModelBo bo) {
        ChatModel update = MapstructUtils.convert(bo, ChatModel.class);
        if (update != null) {
            validEntityBeforeSave(update);
        }
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ChatModel entity) {
        // 判断是否包含*号
        if (entity.getApiKey().contains("*")) {
            // 重新设置key信息
            entity.setApiKey(baseMapper.selectById(entity.getId()).getApiKey());
        }
    }

    /**
     * 批量删除聊天模型
     */
    @Override
    @CacheEvict(value = CacheNames.CHAT_MODEL)
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 通过模型名称获取模型信息
     */
    @Override
    @Cacheable(value = CacheNames.CHAT_MODEL, key = "#modelName", unless = "#result == null")
    public ChatModel selectModelByName(String modelName) {
        return baseMapper.selectOne(Wrappers.<ChatModel>lambdaQuery().eq(ChatModel::getModelName, modelName));
    }

    /**
     * 通过模型分类获取模型信息
     */
    @Override
    public ChatModel selectModelByCategory(String category) {
        return baseMapper.selectOne(Wrappers.<ChatModel>lambdaQuery().eq(ChatModel::getCategory, category));
    }

    /**
     * 通过模型分类获取优先级最高的模型信息
     */
    @Override
    public ChatModel selectModelByCategoryWithHighestPriority(String category) {
        return baseMapper.selectOne(
                Wrappers.<ChatModel>lambdaQuery()
                        .eq(ChatModel::getCategory, category)
                        .orderByDesc(ChatModel::getPriority),
                false
        );
    }

    /**
     * 在同一分类下，查找优先级小于当前优先级的最高优先级模型（用于降级）。
     */
    @Override
    public ChatModel selectFallbackModelByCategoryAndLessPriority(String category, Integer currentPriority) {
        return baseMapper.selectOne(
                Wrappers.<ChatModel>lambdaQuery()
                        .eq(ChatModel::getCategory, category)
                        .lt(ChatModel::getPriority, currentPriority)
                        .orderByDesc(ChatModel::getPriority),
                false
        );
    }
}
