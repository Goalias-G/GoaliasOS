package com.goalias.chat.domain.bo;

import com.goalias.common.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.core.validate.EditGroup;

import java.math.BigDecimal;

/**
 * 支付订单业务对象 chat_pay_order
 *
 * @author Goalias
 * @since 2026-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatPayOrderBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderNo;

    /**
     * 订单名称
     */
    @NotBlank(message = "订单名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String orderName;

    /**
     * 金额
     */
    @NotNull(message = "金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal amount;

    /**
     * 支付状态
     */
    @NotBlank(message = "支付状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private String paymentStatus;

    /**
     * 支付方式
     */
    @NotBlank(message = "支付方式不能为空", groups = { AddGroup.class, EditGroup.class })
    private String paymentMethod;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;

    /**
     * 备注
     */
    @NotBlank(message = "备注不能为空", groups = { AddGroup.class, EditGroup.class })
    private String remark;


}
