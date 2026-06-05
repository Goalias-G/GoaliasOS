package com.goalias.common.schedule.event;

import com.goalias.common.schedule.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 任务快照（事件中传递的最小任务信息）
 *
 * @author Goalias
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSnapshot implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String taskName;
    private TaskType taskType;
    private String cronExpression;
    private String description;

    /**
     * 任务自定义参数（已解析为 Map）
     */
    @Builder.Default
    private Map<String, Object> params = new HashMap<>();
}
