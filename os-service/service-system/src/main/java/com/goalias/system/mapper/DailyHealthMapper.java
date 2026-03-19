package com.goalias.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goalias.system.domain.DailyHealth;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日健康记录Mapper接口
 *
 * @author Goalias
 */
@Mapper
public interface DailyHealthMapper extends BaseMapper<DailyHealth> {

}
