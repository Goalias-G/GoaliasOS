package com.goalias.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 生活记录视图对象 life_record
 *
 * @author Goalias
 */
@Data
public class LifeRecordCountVo implements Serializable {

    private Long categoryId;

    private Integer count;


}
