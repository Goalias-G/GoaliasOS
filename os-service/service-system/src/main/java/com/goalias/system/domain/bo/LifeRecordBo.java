package com.goalias.system.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goalias.common.core.validate.EditGroup;
import com.goalias.common.core.validate.AddGroup;
import com.goalias.common.web.domain.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

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

    /**
     * 记录日期筛选起点（含），GET 查询参数需 DateTimeFormat（非 JsonFormat）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date beginDate;

    /**
     * 记录日期筛选终点（含），GET 查询参数需 DateTimeFormat（非 JsonFormat）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    private String attachsId;

    private Integer favoriteFlag;

    private String remark;

}
