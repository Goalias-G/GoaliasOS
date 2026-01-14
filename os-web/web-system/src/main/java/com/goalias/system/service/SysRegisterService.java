package com.goalias.system.service;

import cn.dev33.satoken.secure.BCrypt;
import com.goalias.common.core.constant.Constants;
import com.goalias.common.core.domain.model.RegisterBody;
import com.goalias.common.core.exception.base.BaseException;
import com.goalias.common.core.exception.user.CaptchaException;
import com.goalias.common.core.exception.user.CaptchaExpireException;
import com.goalias.common.core.exception.user.UserException;
import com.goalias.common.core.utils.ServletUtils;
import com.goalias.common.core.utils.SpringUtils;
import com.goalias.common.core.utils.StringUtils;
import com.goalias.common.redis.constant.GlobalConstants;
import com.goalias.common.redis.service.RedisService;
import com.goalias.system.domain.LogininforEvent;
import com.goalias.system.domain.SysUser;
import com.goalias.system.domain.bo.SysUserBo;
import com.goalias.system.domain.vo.SysUserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 注册校验方法
 *
 * @author Goalias
 */
@RequiredArgsConstructor
@Service
public class SysRegisterService {

    private final ISysUserService userService;

    private final RedisService redisService;


    /**
     * 注册
     */
    public void register(RegisterBody registerBody) {


        String username = registerBody.getUsername();
        String password = registerBody.getPassword();

        // 检查验证码是否正确
        validateEmail(username,registerBody.getCode());
        SysUserBo sysUser = new SysUserBo();
        sysUser.setDomainName(registerBody.getDomainName());
        sysUser.setUserName(username);
        sysUser.setNickName(username);
        sysUser.setPassword(BCrypt.hashpw(password));
        if (!userService.checkUserNameUnique(sysUser)) {
            throw new UserException("添加用户失败", username);
        }
        sysUser.setUserBalance(1.0);
        SysUser user = userService.registerUser(sysUser);
        if (user == null) {
            throw new UserException("用户注册失败!");
        }
        recordLogininfor( username, Constants.REGISTER, "注册成功");
    }

    /**
     * 重置密码
     */
    public void resetPassWord(RegisterBody registerBody) {
        String username = registerBody.getUsername();
        String password = registerBody.getPassword();
        SysUserVo user = userService.selectUserByUserName(username);
        if(user == null){
            throw new UserException(String.format("用户【%s】,未注册!",username));
        }
        // 检查验证码是否正确
        validateEmail(username,registerBody.getCode());
        userService.resetUserPwd(user.getUserId(),BCrypt.hashpw(password));
    }

    /**
     * 校验邮箱验证码
     *
     * @param username 用户名
     */
    public void validateEmail(String username,String code) {
        String key = GlobalConstants.CAPTCHA_CODE_KEY + username;
        String captcha = (String) redisService.get(key);
        if(code.equals(captcha)){
            redisService.del(captcha);
        }else {
            throw new UserException("验证码错误,请重试！");
        }
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     */
    public void validateCaptcha( String username, String code, String uuid) {
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + StringUtils.defaultString(uuid, "");
        String captcha = (String) redisService.get(verifyKey);
        redisService.del(verifyKey);
        if (captcha == null) {
            recordLogininfor( username, Constants.REGISTER, "验证码已过期");
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            recordLogininfor( username, Constants.REGISTER, "验证码错误,请重试");
            throw new CaptchaException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status   状态
     * @param message  消息内容
     * @return
     */
    private void recordLogininfor( String username, String status, String message) {
        LogininforEvent logininforEvent = new LogininforEvent();
        logininforEvent.setUsername(username);
        logininforEvent.setStatus(status);
        logininforEvent.setMessage(message);
        logininforEvent.setRequest(ServletUtils.getRequest());
        SpringUtils.context().publishEvent(logininforEvent);
    }

}
