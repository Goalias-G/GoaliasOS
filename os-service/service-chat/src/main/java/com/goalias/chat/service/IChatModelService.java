package com.goalias.chat.service;

import com.goalias.chat.domain.ChatModel;
import com.goalias.chat.domain.bo.ChatModelBo;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 聊天模型Service接口
 *
 * @author Goalias
 * @since 2026-01-16
 */
public interface IChatModelService {

    /**
     * 查询聊天模型
     */
    ChatModel queryById(Long id);

    /**
     * 查询聊天模型列表
     */
    TableDataInfo<ChatModel> queryPageList(ChatModelBo bo, PageQuery pageQuery);

    /**
     * 查询聊天模型列表
     */
    List<ChatModel> queryList(ChatModelBo bo);

    /**
     * 新增聊天模型
     */
    Boolean insertByBo(ChatModelBo bo);

    /**
     * 修改聊天模型
     */
    Boolean updateByBo(ChatModelBo bo);

    /**
     * 校验并批量删除聊天模型信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);


    /**
     * 通过模型名称获取模型信息
     */
    ChatModel selectModelByName(String modelName);
    /**
     * 通过模型分类获取模型信息
     */
    ChatModel selectModelByCategory(String image);
    
    /**
     * 通过模型分类获取优先级最高的模型信息
     */
    ChatModel selectModelByCategoryWithHighestPriority(String category);
    
    /**
     * 在同一分类下，查找优先级小于当前优先级的最高优先级模型（用于降级）。
     */
    ChatModel selectFallbackModelByCategoryAndLessPriority(String category, Integer currentPriority);
    
    /**
     * 获取ppt模型信息
     */
    ChatModel getPPT();

}
