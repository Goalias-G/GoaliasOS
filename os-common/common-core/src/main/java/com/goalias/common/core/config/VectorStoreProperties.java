package com.goalias.common.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 向量库配置属性
 *
 * @author Goalias
 */
@Data
@Component
@ConfigurationProperties(prefix = "vector-store")
public class VectorStoreProperties {

    /**
     * 向量库类型
     */
    private String type;

    /**
     * PineCore配置
     */
    private PineCore pinecore = new PineCore();

    /**
     * Milvus配置
     */
    private Milvus milvus = new Milvus();

    @Data
    public static class PineCore {
        /**
         * 连接URL
         */
        private String url;
    }

    @Data
    public static class Milvus {
        /**
         * 连接URL
         */
        private String url;

        /**
         * 集合名称
         */
        private String collectionname;
    }
}