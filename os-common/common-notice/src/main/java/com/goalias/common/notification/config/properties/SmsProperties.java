package com.goalias.common.notification.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SMS短信 配置属性
 *
 * @author Goalias
 * @version 4.2.0
 */
@Data
@ConfigurationProperties(prefix = "notice.sms")
public class SmsProperties {

    private Boolean enabled;

    /**
     * 配置节点
     * 阿里云 dysmsapi.aliyuncs.com
     * 腾讯云 sms.tencentcloudapi.com
     */
    private String endpoint = "dysmsapi.aliyuncs.com";

    /**
     * key
     */
    private String accessKeyId;

    /**
     * 密匙
     */
    private String accessKeySecret;

    /*
     * 短信签名
     */
    private String signName;

}
