package com.goalias.chat.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;




/**
 * 支付订单视图对象 chat_pay_order
 *
 * @author Goalias
 * @since 2026-01-16
 */
@Data
public class ChatPayOrderVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 订单名称
     */
    private String orderName;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 支付状态
     */
    private String paymentStatus;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 备注
     */
    private String remark;


}
