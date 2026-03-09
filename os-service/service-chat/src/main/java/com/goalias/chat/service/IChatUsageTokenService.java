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
 * @since 2026-01-22 */
public interface IChatUsageTokenService {

    /**
     * 查询用户token使用详情
     */
    ChatUsageToken queryById(Long id);

    /**
     * 根据模型名称查询使用量
     */
    ChatUsageToken queryByModelName(String modelName);

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

    /**
     * 从数据库加载所有记录到Redis
     */
    void loadToRedis();

    /**
     * 从Redis同步到数据库（每6小时定时任务）
     */
    void syncToDatabase();
}
