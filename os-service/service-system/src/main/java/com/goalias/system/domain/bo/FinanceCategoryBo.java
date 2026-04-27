package com.goalias.system.domain.bo;

import com.goalias.common.core.validate.EditGroup;
import com.goalias.common.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
public class FinanceCategoryBo extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    private Long userId;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotNull(message = "分类类型不能为空")
    private Integer type;

    private String icon;
}
