package com.goalias.knowledge.domain.bo;

import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.core.validate.EditGroup;
import com.goalias.common.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识片段业务对象 knowledge_fragment
 *
 * @author Goalias
 * @since 2026-01-22 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeFragmentBo extends BaseEntity {

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 知识库ID
     */
    @NotBlank(message = "知识库ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String kid;

    /**
     * 文档ID
     */
    @NotBlank(message = "文档ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String docId;

    /**
     * 知识片段ID
     */
    @NotBlank(message = "知识片段ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fid;

    /**
     * 片段索引下标
     */
    @NotNull(message = "片段索引下标不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long idx;

    /**
     * 文档内容
     */
    @NotBlank(message = "文档内容不能为空", groups = { AddGroup.class, EditGroup.class })
    private String content;

    /**
     * 备注
     */
    @NotBlank(message = "备注不能为空", groups = { AddGroup.class, EditGroup.class })
    private String remark;


}
