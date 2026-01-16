package com.goalias.chat.service;

import com.goalias.chat.domain.ChatPayOrder;
import com.goalias.chat.domain.bo.ChatPayOrderBo;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 支付订单Service接口
 *
 * @author Goalias
 * @since 2026-01-16
 */
public interface IChatPayOrderService {

    /**
     * 查询支付订单
     */
    ChatPayOrder queryById(Long id);

    /**
     * 查询支付订单列表
     */
    TableDataInfo<ChatPayOrder> queryPageList(ChatPayOrderBo bo, PageQuery pageQuery);

    /**
     * 查询支付订单列表
     */
    List<ChatPayOrder> queryList(ChatPayOrderBo bo);

    /**
     * 新增支付订单
     */
    Boolean insertByBo(ChatPayOrderBo bo);

    /**
     * 修改支付订单
     */
    Boolean updateByBo(ChatPayOrderBo bo);

    /**
     * 校验并批量删除支付订单信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
