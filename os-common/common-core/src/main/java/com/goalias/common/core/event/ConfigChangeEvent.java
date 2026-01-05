package com.goalias.common.core.event;

import org.springframework.context.ApplicationEvent;

/**
 *  定义一个事件类，用于通知配置变化
 *
 * @author Goalias
 * date 2024/5/19
 */
public class ConfigChangeEvent extends ApplicationEvent {
    public ConfigChangeEvent(Object source) {
        super(source);
    }
}
