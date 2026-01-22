package com.goalias.common.chat.openai.function;

import java.util.function.Function;

/**
 *  key 的获取策略
 * jdk默认实现
 * @see Function
 *
 * @author Goalias
 * @since 2026-01-22 */
@FunctionalInterface
public interface KeyStrategyFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t);

}
