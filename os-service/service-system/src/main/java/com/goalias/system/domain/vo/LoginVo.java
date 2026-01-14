package com.goalias.system.domain.vo;

import lombok.Data;
import com.goalias.common.core.domain.model.LoginUser;

/**
 * 登录返回信息
 *
 * @author Goalias
 */
@Data
public class LoginVo {
    private String token;
    private LoginUser userInfo;
}
