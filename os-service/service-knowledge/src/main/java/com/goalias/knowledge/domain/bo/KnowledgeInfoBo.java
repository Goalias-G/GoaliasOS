package com.goalias.knowledge.domain.bo;

import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.core.validate.EditGroup;
import com.goalias.common.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库业务对象 knowledge_info
 *
 * @author Goalias
 * @since 2026-01-22 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeInfoBo extends BaseEntity {

    /**
     *  主键
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 知识库ID
     */
    @NotBlank(message = "知识库ID不能为空", groups = {EditGroup.class })
    private String kid;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = {EditGroup.class })
    private Long uid;

    /**
     * 知识库名称
     */
    @NotBlank(message = "知识库名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String kname;

    /**
     * 描述
     */
    private String description;

    /**
     * 知识分隔符
     */
    private String knowledgeSeparator;

    /**
     * 提问分隔符
     */
    private String questionSeparator;

    /**
     * 重叠字符数
     */
    private Long overlapChar;

    /**
     * 知识库中检索的条数
     */
    @NotNull(message = "知识库中检索的条数不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long retrieveLimit;

    /**
     * 文本块大小
     */
    private Long textBlockSize;

    /**
     * 向量化模型id
     */
    private Long embeddingModelId;

    /**
     * 向量化模型名称
     */
    private String embeddingModelName;

    /**
     * 备注
     */
    private String remark;

}
