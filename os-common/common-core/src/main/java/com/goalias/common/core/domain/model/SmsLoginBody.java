package com.goalias.common.core.domain.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 短信登录对象
 *
 * @author Goalias
 */

@Data
public class SmsLoginBody {

    /**
     * 手机号
     */
    @NotBlank(message = "{user.phonenumber.not.blank}")
    private String phonenumber;

    /**
     * 短信code
     */
    @NotBlank(message = "{sms.code.not.blank}")
    private String smsCode;

}
