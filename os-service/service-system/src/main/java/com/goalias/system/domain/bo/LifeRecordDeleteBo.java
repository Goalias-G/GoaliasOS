package com.goalias.system.domain.bo;

import lombok.Data;

import java.util.List;

/**
 * 生活记录业务对象 life_record
 *
 * @author Goalias
 */
@Data
public class LifeRecordDeleteBo {

    private List<Long> ids;

    private List<Long> fileIds;

}
