package com.goalias.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.goalias.system.domain.event.SysLogininfor;
import org.apache.ibatis.annotations.Mapper;


/**
 * 系统访问日志情况信息 数据层
 *
 * @author Goalias
 */
@Mapper
public interface SysLogininforMapper extends BaseMapper<SysLogininfor> {

}
