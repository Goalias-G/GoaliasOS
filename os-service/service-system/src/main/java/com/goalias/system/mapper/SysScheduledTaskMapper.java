package com.goalias.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.system.domain.SysScheduledTask;
import com.goalias.system.domain.vo.SysScheduledTaskVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysScheduledTaskMapper extends BaseMapper<SysScheduledTask> {

    /**
     * 分页查询任务列表
     */
    Page<SysScheduledTaskVo> selectTaskPage(IPage<SysScheduledTaskVo> page,
                                            @Param("userId") Long userId,
                                            @Param("taskName") String taskName,
                                            @Param("taskType") String taskType,
                                            @Param("status") String status);

    /**
     * 查询状态为运行中的全部任务（启动加载使用）
     */
    List<SysScheduledTask> selectRunningTasks();

    /**
     * 根据ID列表查询任务
     */
    List<SysScheduledTask> selectByIds(@Param("ids") List<Long> ids);
}
