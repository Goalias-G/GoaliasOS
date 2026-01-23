package com.goalias.chat.chat.factory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.goalias.chat.chat.tools.OsTool;
import com.goalias.chat.chat.tools.OsToolParam;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema;
import dev.langchain4j.model.chat.request.json.JsonNumberSchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;

@Component
public class FunctionCallsFactory {

    public static List<ToolSpecification> buildTools(Object toolInstance) {
        List<ToolSpecification> specs = new ArrayList<>();
        Class<?> clazz = toolInstance.getClass();

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(OsTool.class)) {
                OsTool OsTool = method.getAnnotation(OsTool.class);
                ToolSpecification.Builder specBuilder = ToolSpecification.builder()
                        .name(OsTool.name().isEmpty() ? method.getName() : OsTool.name())
                        .description(OsTool.description());

                // 构建参数
                JsonObjectSchema.Builder paramsBuilder = JsonObjectSchema.builder();
                List<String> requiredParam = new ArrayList<>();
                for (Parameter param : method.getParameters()) {
                    OsToolParam toolParam = param.getAnnotation(OsToolParam.class);
                    if (toolParam != null) {
                        paramsBuilder.addProperty(
                                toolParam.name(),
                                getToolParamDesc(param.getType(),toolParam.description())
                        );
                        if (toolParam.required()) {
                            requiredParam.add(toolParam.name());
                        }
                    }
                }
                specBuilder.parameters(paramsBuilder.required(requiredParam).build());
                specs.add(specBuilder.build());
            }
        }
        return specs;
    }

    private static JsonSchemaElement getToolParamDesc(Class<?> type, String description) {
        if (type == String.class)
            return JsonStringSchema.builder().description(description).build();
        if (type == Integer.class || type == int.class || type == Long.class || type == long.class || type == double.class || type == Double.class)
            return JsonNumberSchema.builder().description(description).build();
        if (type == Boolean.class || type == boolean.class)
            return JsonBooleanSchema.builder().description(description).build();
        return JsonStringSchema.builder().description(description).build(); // 默认
    }
}
