package com.goalias.chat.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.goalias.common.web.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 提示词模板对象 prompt_template
 *
 * @author Goalias
 * @since 2026-01-22 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prompt_template")
public class PromptTemplate extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 提示词模板名称
     */
    private String templateName;

    /**
     * 提示词模板内容
     */
    private String templateContent;

    /**
     * 提示词分类: promptTemplateEnum
     */
    private String category;

    /**
     * 提示词优先级
     */
    private Integer priority;

    /**
     * 备注
     */
    private String remark;

}