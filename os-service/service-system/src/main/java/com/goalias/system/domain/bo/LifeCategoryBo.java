package com.goalias.system.domain.bo;

import com.goalias.common.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 生活分类业务对象 life_category
 *
 * @author Goalias
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LifeCategoryBo extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Integer sortOrder;

}
