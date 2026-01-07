package com.goalias.common.notification.core;

import cn.hutool.json.JSONUtil;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.goalias.common.core.exception.UtilException;
import com.goalias.common.core.utils.StringUtils;
import com.goalias.common.notification.config.properties.SmsProperties;
import com.goalias.common.notification.entity.SmsResult;
import lombok.SneakyThrows;


import java.security.SecureRandom;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Aliyun 短信模板
 *
 * @author Goalias
 * @version 4.2.0
 */
public class AliyunSmsTemplate implements SmsTemplate {

    private final SmsProperties properties;

    private final Client client;

    @SneakyThrows(Exception.class)
    public AliyunSmsTemplate(SmsProperties smsProperties) {
        this.properties = smsProperties;
        Config config = new Config()
            // 您的AccessKey ID
            .setAccessKeyId(smsProperties.getAccessKeyId())
            // 您的AccessKey Secret
            .setAccessKeySecret(smsProperties.getAccessKeySecret())
            // 访问的域名
            .setEndpoint(smsProperties.getEndpoint());
        this.client = new Client(config);
    }

    private static final String NUMBERS = "0123456789";
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public SmsResult send(String phones, String templateId, Map<String, String> param) {
        if (StringUtils.isBlank(phones)) {
            throw new UtilException("手机号不能为空");
        }
        for (String phone : phones.split(",")) {
            if (!isMobileNum(phone)) {
                throw new UtilException("手机号格式不正确");
            }
        }
        if (StringUtils.isBlank(templateId)) {
            throw new UtilException("模板ID不能为空");
        }
        SendSmsRequest req = new SendSmsRequest()
            .setPhoneNumbers(phones)
            .setSignName(properties.getSignName())
            .setTemplateCode(templateId)
            .setTemplateParam(JSONUtil.toJsonStr(param));
        try {
            SendSmsResponse resp = client.sendSms(req);
            return SmsResult.builder()
                .isSuccess("OK".equals(resp.getBody().getCode()))
                .message(resp.getBody().getMessage())
                .response(JSONUtil.toJsonStr(resp))
                .build();
        } catch (Exception e) {
            throw new UtilException(e.getMessage());
        }
    }

    /**
     * 检测手机号有效性*
     * @param mobile 手机号码
     * @return 是否有效
     */
    public static boolean isMobileNum(String mobile){
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobile);
        return m.matches();
    }

    public static String randomSMSCode(int length, boolean numberFlag) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        String chars = numberFlag ? NUMBERS : ALPHANUMERIC;
        char[] buffer = new char[length];

        // 直接填充 char[]，避免 StringBuilder/String 拼接开销
        for (int i = 0; i < length; i++) {
            buffer[i] = chars.charAt(SECURE_RANDOM.nextInt(chars.length()));
        }

        return new String(buffer);
    }

}
