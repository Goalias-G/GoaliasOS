package com.goalias.chat.service;


import com.goalias.chat.domain.ChatUsageToken;
import com.goalias.chat.domain.bo.ChatUsageTokenBo;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 用户token使用详情Service接口
 *
 * @author Goalias
 * @since 2026-01-16
 */
public interface IChatUsageTokenService {

    /**
     * 查询用户token使用详情
     */
    ChatUsageToken queryById(Long id);

    /**
     * 查询用户token使用详情列表
     */
    TableDataInfo<ChatUsageToken> queryPageList(ChatUsageTokenBo bo, PageQuery pageQuery);

    /**
     * 查询用户token使用详情列表
     */
    List<ChatUsageToken> queryList(ChatUsageTokenBo bo);

    /**
     * 新增用户token使用详情
     */
    Boolean insertByBo(ChatUsageTokenBo bo);

    /**
     * 修改用户token使用详情
     */
    Boolean updateByBo(ChatUsageTokenBo bo);

    /**
     * 校验并批量删除用户token使用详情信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
