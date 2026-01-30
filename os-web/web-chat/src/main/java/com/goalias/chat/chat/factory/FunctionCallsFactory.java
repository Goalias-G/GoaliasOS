package com.goalias.chat.chat.factory;

import com.goalias.chat.chat.tools.OsTool;
import com.goalias.chat.chat.tools.OsToolParam;
import com.goalias.chat.chat.tools.OsToolProvider;
import com.goalias.chat.chat.tools.ToolMetadata;
import com.goalias.common.core.utils.SpringUtils;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具声明工厂
 * 负责扫描、注册和管理所有 AI 可调用的工具
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FunctionCallsFactory {


    /**
     * 工具注册表：工具名称 -> 工具元数据
     */
    private final Map<String, ToolMetadata> toolRegistry = new ConcurrentHashMap<>();

    @Getter
    private final List<ToolSpecification> toolSpecifications = new ArrayList<>();

    /**
     * 扫描并构建所有工具规范
     */
    @EventListener(ApplicationReadyEvent.class)
    public void scanAndBuildTools() {
        toolRegistry.clear();

        Map<String, OsToolProvider> toolProviders = SpringUtils.getBeansOfType(OsToolProvider.class);

        for (OsToolProvider provider : toolProviders.values()) {
            try {
                Class<?> clazz = provider.getClass();
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(OsTool.class)) {
                        try {
                            ToolSpecification spec = buildToolSpec(method, provider);
                            toolSpecifications.add(spec);
                            log.debug("注册工具: {}", spec.name());
                        } catch (Exception e) {
                            log.error("构建工具规范失败: {}.{}", clazz.getName(), method.getName(), e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("扫描工具提供者 {} 的工具时发生错误", provider.getClass().getName(), e);
            }
        }

        log.info("工具扫描完成，共发现 {} 的 {} 个Tool", toolProviders.keySet(), toolRegistry.size());
    }

    /**
     * 构建单个工具的规范
     *
     * @param method   工具方法
     * @param instance 工具实例
     * @return 工具规范
     */
    public ToolSpecification buildToolSpec(Method method, Object instance) {
        OsTool osTool = method.getAnnotation(OsTool.class);
        String toolName = osTool.name().isEmpty() ? method.getName() : osTool.name();

        // 构建工具规范
        ToolSpecification.Builder specBuilder = ToolSpecification.builder()
                .name(toolName)
                .description(osTool.description());

        // 构建参数 Schema
        JsonObjectSchema.Builder paramsBuilder = JsonObjectSchema.builder();
        List<String> requiredParams = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        String[] parameterNames = new String[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            OsToolParam toolParam = param.getAnnotation(OsToolParam.class);

            if (toolParam != null) {
                String paramName = toolParam.name();
                parameterNames[i] = paramName;

                // 解析参数类型（支持复杂类型）
                JsonSchemaElement schema = parseParameterType(param.getType(), toolParam.description());
                paramsBuilder.addProperty(paramName, schema);

                if (toolParam.required()) {
                    requiredParams.add(paramName);
                }
            }
        }

        specBuilder.parameters(paramsBuilder.required(requiredParams).build());

        // 注册工具元数据
        ToolMetadata metadata = ToolMetadata.builder()
                .name(toolName)
                .description(osTool.description())
                .method(method)
                .instance(instance)
                .parameterTypes(method.getParameterTypes())
                .parameterNames(parameterNames)
                .build();

        toolRegistry.put(toolName, metadata);

        return specBuilder.build();
    }

    /**
     * 解析参数类型，支持基本类型、POJO 和集合
     *
     * @param type        参数类型
     * @param description 参数描述
     * @return JSON Schema 元素
     */
    private JsonSchemaElement parseParameterType(Class<?> type, String description) {
        // 基本类型
        if (type == String.class) {
            return JsonStringSchema.builder().description(description).build();
        }
        if (type == Integer.class || type == int.class ||
                type == Long.class || type == long.class ||
                type == Double.class || type == double.class ||
                type == Float.class || type == float.class) {
            return JsonNumberSchema.builder().description(description).build();
        }
        if (type == Boolean.class || type == boolean.class) {
            return JsonBooleanSchema.builder().description(description).build();
        }

        // 数组类型
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            JsonSchemaElement itemSchema = parseParameterType(componentType, "数组元素");
            return JsonArraySchema.builder()
                    .description(description)
                    .items(itemSchema)
                    .build();
        }

        // List 类型（简化处理，假设元素为 String)
        if (List.class.isAssignableFrom(type)) {
            return JsonArraySchema.builder()
                    .description(description)
                    .items(JsonStringSchema.builder().build())
                    .build();
        }

        // POJO 类型
        if (!type.isPrimitive() && !type.getName().startsWith("java.")) {
            return parsePojoType(type, description);
        }

        // 默认为字符串
        return JsonStringSchema.builder().description(description).build();
    }

    /**
     * 递归解析 POJO 类型
     *
     * @param pojoType    POJO 类型
     * @param description 描述
     * @return JSON Object Schema
     */
    private JsonObjectSchema parsePojoType(Class<?> pojoType, String description) {
        JsonObjectSchema.Builder builder = JsonObjectSchema.builder()
                .description(description);

        // 获取所有字段
        Field[] fields = pojoType.getDeclaredFields();
        for (Field field : fields) {
            // 跳过静态字段和合成字段
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                    field.isSynthetic()) {
                continue;
            }

            String fieldName = field.getName();
            Class<?> fieldType = field.getType();

            // 递归解析字段类型
            JsonSchemaElement fieldSchema = parseParameterType(fieldType, fieldName);
            builder.addProperty(fieldName, fieldSchema);
        }

        return builder.build();
    }

    /**
     * 获取工具元数据
     *
     * @param toolName 工具名称
     * @return 工具元数据，如果不存在返回 null
     */
    public ToolMetadata getToolMetadata(String toolName) {
        return toolRegistry.get(toolName);
    }

    /**
     * 获取所有已注册的工具名称
     *
     * @return 工具名称列表
     */
    public List<String> getAllToolNames() {
        return new ArrayList<>(toolRegistry.keySet());
    }

    /**
     * 检查工具是否已注册
     *
     * @param toolName 工具名称
     * @return 是否已注册
     */
    public boolean isToolRegistered(String toolName) {
        return toolRegistry.containsKey(toolName);
    }

    /**
     * 获取工具注册表大小
     *
     * @return 已注册工具数量
     */
    public int getToolCount() {
        return toolRegistry.size();
    }

}
