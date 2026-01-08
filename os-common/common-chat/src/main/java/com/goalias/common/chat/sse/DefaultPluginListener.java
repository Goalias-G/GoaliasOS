package com.goalias.common.chat.sse;

import lombok.extern.slf4j.Slf4j;
import okhttp3.sse.EventSourceListener;
import com.goalias.common.chat.entity.chat.ChatCompletion;
import com.goalias.common.chat.openai.OpenAiStreamClient;
import com.goalias.common.chat.openai.plugin.PluginAbstract;

/**
 *   插件开发返回信息收集sse监听器
 *
 * @author https:www.unfbx.com
 * 2023-08-18
 */
@Slf4j
public class DefaultPluginListener extends PluginListener {

    public DefaultPluginListener(OpenAiStreamClient client, EventSourceListener eventSourceListener, PluginAbstract plugin, ChatCompletion chatCompletion) {
        super(client, eventSourceListener, plugin, chatCompletion);
    }
}
