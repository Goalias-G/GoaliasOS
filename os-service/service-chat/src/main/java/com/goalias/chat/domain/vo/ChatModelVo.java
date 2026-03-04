package com.goalias.chat.domain.vo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.goalias.common.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;


/**
 * 聊天模型对象 chat_model
 *
 * @author Goalias
 * @since 2026-01-22 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_model")
public class ChatModelVo extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 模型分类
     */
    private String category;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型描述
     */
    private String modelDescribe;


    /**
     * 计费类型
     */
    private String modelType;

    private Double modelPrice;

    /**
     * 是否显示
     */
    private String modelShow;

    /**
     * 是否支持联网搜索(0-否 1-是)
     */
    private Integer enableSearch;


    /**
     * 模型供应商
     */
    private String providerName;

    /**
     * 备注
     */
    private String remark;



}
