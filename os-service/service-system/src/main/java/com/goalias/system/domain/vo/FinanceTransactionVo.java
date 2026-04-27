package com.goalias.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class FinanceTransactionVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private Long categoryId;

    private Long amount;

    private Integer tag;

    private String remark;

    private Date createTime;

    private Date updateTime;

    private String categoryName;

    private Integer categoryType;
}
