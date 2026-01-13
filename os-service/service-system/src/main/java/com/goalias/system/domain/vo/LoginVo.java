package com.goalias.system.domain.vo;

import lombok.Data;
import com.goalias.common.core.domain.model.LoginUser;

/**
 * 登录返回信息
 *
 * @author Michelle.Chung
 */
@Data
public class LoginVo {
    private String token;
    // 兼容新版后台管理系统
    private String access_token;
    private LoginUser userInfo;
}
