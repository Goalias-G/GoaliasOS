package com.goalias.common.chat.openai.function;

import cn.hutool.core.util.RandomUtil;

import java.util.List;

/**
 *  随机策略
 *
 * @author Goalias
 * @since 2026-01-22 */
public class KeyRandomStrategy implements KeyStrategyFunction<List<String>, String> {

    @Override
    public String apply(List<String> apiKeys) {
        return RandomUtil.randomEle(apiKeys);
    }
}
