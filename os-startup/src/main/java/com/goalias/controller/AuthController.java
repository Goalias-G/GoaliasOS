package com.goalias.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.goalias.common.core.domain.R;
import com.goalias.common.core.domain.model.LoginBody;
import com.goalias.common.core.domain.model.RegisterBody;
import com.goalias.common.core.domain.model.VisitorLoginBody;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.system.domain.vo.LoginVo;
import com.goalias.system.service.SysLoginService;
import com.goalias.system.service.SysRegisterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证
 *
 * @author Goalias
 */
@SaIgnore
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SysLoginService loginService;
    private final SysRegisterService registerService;

    /**
     * 登录方法
     *
     * @param body 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public R<LoginVo> login(@Validated @RequestBody LoginBody body) {
        LoginVo loginVo = new LoginVo();
        // 生成令牌
        String token = loginService.login(
                body.getUsername(), body.getPassword(),
                body.getCode(), body.getUuid());
        loginVo.setToken(token);
        loginVo.setUserInfo(LoginHelper.getLoginUser());
        return R.ok(loginVo);
    }


    /**
     * 访客登录
     * @param loginBody 登录信息
     * @return token信息
     */
    @PostMapping("/visitorLogin")
    public R<LoginVo> visitorLogin(@RequestBody VisitorLoginBody loginBody) {
        LoginVo loginVo = new LoginVo();
        return R.ok(loginVo);
    }


    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        loginService.logout();
        return R.ok("退出成功");
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public R<Void> register(@Validated @RequestBody RegisterBody user, HttpServletRequest request) {
        String domainName =  request.getServerName();
        user.setDomainName(domainName);
        registerService.register(user);
        return R.ok();
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset/password")
    @SaIgnore
    public R<Void> resetPassWord(@Validated @RequestBody RegisterBody user) {
        registerService.resetPassWord(user);
        return R.ok();
    }

}
