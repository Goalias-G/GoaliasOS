package com.goalias.common.redis.constant;

/**
 * 缓存组名称常量
 * <p>
 * key 格式为 cacheNames#ttl#maxIdleTime#maxSize
 * <p>
 * ttl 过期时间 如果设置为0则不过期 默认为0
 * maxIdleTime 最大空闲时间 根据LRU算法清理空闲数据 如果设置为0则不检测 默认为0
 * maxSize 组最大长度 根据LRU算法清理溢出数据 如果设置为0则无限长 默认为0
 * <p>
 * 例子: test#60s、test#0#60s、test#0#1m#1000、test#1h#0#500
 *
 * @author Goalias
 */
public interface CacheNames {


    /**
     * 系统配置
     */
    String SYS_CONFIG = "sys_config";

    /**
     * 用户账户
     */
    String SYS_USER = "sys_user";

    /**
     * OSS内容
     */
    String SYS_OSS = "sys_oss";

     /**
     * 知识库信息
     */
    String KNOWLEDGE_INFO = "knowledge_info";

    /**
     * 聊天Token信息
     */
    String CHAT_TOKEN_INPUT = "chat_token_input:";

    String CHAT_TOKEN_OUTPUT = "chat_token_output:";

    String CHAT_PROMPT = "chat_prompt";

    String CHAT_MODEL = "chat_model";


}
