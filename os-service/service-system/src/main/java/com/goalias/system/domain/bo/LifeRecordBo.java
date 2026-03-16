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
 * 生活记录业务对象 life_record
 *
 * @author Goalias
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LifeRecordBo extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    private Long userId;

    private Long categoryId;

    private String title;

    private String content;

    private Integer rating;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date recordDate;

    private String attachsId;

    private Integer favoriteFlag;

    private String remark;

}
