package com.goalias.chat.chat.tools;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * 工具元数据
 * 存储工具的完整信息，用于工具注册表管理和执行
 */
@Data
@Builder
public class ToolMetadata {
    
    /**
     * 工具名称
     */
    private String name;
    
    /**
     * 工具描述
     */
    private String description;
    
    /**
     * 工具方法
     */
    private Method method;
    
    /**
     * 工具实例（Spring Bean）
     */
    private Object instance;
    
    /**
     * 参数类型列表
     */
    private Class<?>[] parameterTypes;
    
    /**
     * 参数名称列表
     */
    private String[] parameterNames;
    
    /**
     * 是否需要权限验证
     */
    private boolean requiresAuth;
    
    /**
     * 所需权限
     */
    private String[] requiredPermissions;
    
    /**
     * 执行超时时间（毫秒）
     */
    @Builder.Default
    private long timeout = 10000;
    
    /**
     * 是否启用
     */
    @Builder.Default
    private boolean enabled = true;
}
