package com.goalias.chat.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goalias.chat.domain.ChatPayOrder;
import org.apache.ibatis.annotations.Mapper;


/**
 * 支付订单Mapper接口
 *
 * @author Goalias
 * @since 2026-01-16
 */
@Mapper
public interface ChatPayOrderMapper extends BaseMapper<ChatPayOrder> {

}
