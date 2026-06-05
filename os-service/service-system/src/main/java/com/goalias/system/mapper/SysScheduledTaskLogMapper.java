package com.goalias.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.system.domain.SysScheduledTaskLog;
import com.goalias.system.domain.vo.SysScheduledTaskLogVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysScheduledTaskLogMapper extends BaseMapper<SysScheduledTaskLog> {

    /**
     * 分页查询任务执行日志
     */
    Page<SysScheduledTaskLogVo> selectLogPage(IPage<SysScheduledTaskLogVo> page,
                                               @Param("taskId") Long taskId,
                                               @Param("taskName") String taskName,
                                               @Param("taskType") String taskType,
                                               @Param("status") String status,
                                               @Param("source") String source,
                                               @Param("beginTime") String beginTime,
                                               @Param("endTime") String endTime);
}
