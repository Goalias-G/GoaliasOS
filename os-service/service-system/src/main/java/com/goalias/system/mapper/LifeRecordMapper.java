package com.goalias.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goalias.system.domain.LifeRecord;
import com.goalias.system.domain.vo.LifeRecordCountVo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 生活记录Mapper接口
 *
 * @author Goalias
 */
@Mapper
public interface LifeRecordMapper extends BaseMapper<LifeRecord> {

    List<LifeRecordCountVo> queryRecordCountByCategoryId(@Param("categoryIds")List<Long> categoryIds);
}
