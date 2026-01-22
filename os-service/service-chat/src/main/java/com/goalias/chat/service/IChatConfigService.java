package com.goalias.chat.service;


import com.goalias.chat.domain.ChatConfig;
import com.goalias.chat.domain.bo.ChatConfigBo;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 配置信息Service接口
 *
 * @author Goalias
 * @since 2026-01-22 */
public interface IChatConfigService {

    /**
     * 查询配置信息
     */
    ChatConfig queryById(Long id);

    /**
     * 查询配置信息列表
     */
    TableDataInfo<ChatConfig> queryPageList(ChatConfigBo bo, PageQuery pageQuery);

    /**
     * 查询配置信息列表
     */
    List<ChatConfig> queryList(ChatConfigBo bo);

    /**
     * 新增配置信息
     */
    Boolean insertByBo(ChatConfigBo bo);

    /**
     * 修改配置信息
     */
    Boolean updateByBo(ChatConfigBo bo);

    /**
     * 校验并批量删除配置信息信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);


    /**
     * 查询系统参数
     */
    List<ChatConfig> getSysConfigValue(String category);

    String getConfigValue(String category,String configKey);
}
