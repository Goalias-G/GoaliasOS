package com.goalias.system.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goalias.common.core.validate.EditGroup;
import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.web.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 每日知识业务对象 daily_knowledge
 *
 * @author Goalias
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DailyKnowledgeBo extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    private String type;

    private String title;

    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date knowledgeDate;

    private Date startTime;

    private Date endTime;
}
