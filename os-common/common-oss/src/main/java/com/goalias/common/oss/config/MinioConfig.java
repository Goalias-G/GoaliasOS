package com.goalias.common.oss.config;

import com.goalias.common.oss.config.properties.MinioProperties;
import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Minio 配置类
 *
 * @author Goalias
 */
@AutoConfiguration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    /**
     * 创建 MinIO 客户端
     */
    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

}
