package com.goalias.chat.domain.bo;

import com.goalias.common.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.core.validate.EditGroup;


/**
 * 提示词模板业务对象 prompt_template
 *
 * @author Goalias
 * @since 2026-01-22 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PromptTemplateBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 提示词模板名称
     */
    @NotBlank(message = "提示词模板名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String templateName;

    /**
     * 提示词模板内容
     */
    @NotBlank(message = "提示词模板内容不能为空", groups = {AddGroup.class, EditGroup.class})
    private String templateContent;

    /**
     * 提示词分类，knowledge 知识库类型，chat 对话类型，draw绘画类型 ...
     */
    @NotBlank(message = "提示词分类", groups = {AddGroup.class, EditGroup.class})
    private String category;

    /**
     * 备注
     */
    @NotBlank(message = "备注不能为空", groups = {AddGroup.class, EditGroup.class})
    private String remark;
}