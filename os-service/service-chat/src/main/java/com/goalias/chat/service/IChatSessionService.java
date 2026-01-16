package com.goalias.chat.service;

import com.goalias.chat.domain.ChatSession;
import com.goalias.chat.domain.bo.ChatSessionBo;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 会话管理Service接口
 *
 * @author Goalias
 * @since 2026-01-16
 */
public interface IChatSessionService {

    /**
     * 查询会话管理
     */
    ChatSession queryById(Long id);

    /**
     * 查询会话管理列表
     */
    TableDataInfo<ChatSession> queryPageList(ChatSessionBo bo, PageQuery pageQuery);

    /**
     * 查询会话管理列表
     */
    List<ChatSession> queryList(ChatSessionBo bo);

    /**
     * 新增会话管理
     */
    Boolean insertByBo(ChatSessionBo bo);

    /**
     * 修改会话管理
     */
    Boolean updateByBo(ChatSessionBo bo);

    /**
     * 校验并批量删除会话管理信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
