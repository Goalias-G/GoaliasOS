package com.goalias.chat.service;



import com.goalias.chat.domain.ChatMessage;
import com.goalias.chat.domain.bo.ChatMessageBo;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 聊天消息Service接口
 *
 * @author Goalias
 * @since 2026-01-16
 */
public interface IChatMessageService {

    /**
     * 查询聊天消息
     */
    ChatMessage queryById(Long id);

    /**
     * 查询聊天消息列表
     */
    TableDataInfo<ChatMessage> queryPageList(ChatMessageBo bo, PageQuery pageQuery);

    /**
     * 查询聊天消息列表
     */
    List<ChatMessage> queryList(ChatMessageBo bo);

    /**
     * 新增聊天消息
     */
    Boolean insertByBo(ChatMessageBo bo);

    /**
     * 修改聊天消息
     */
    Boolean updateByBo(ChatMessageBo bo);

    /**
     * 校验并批量删除聊天消息信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
