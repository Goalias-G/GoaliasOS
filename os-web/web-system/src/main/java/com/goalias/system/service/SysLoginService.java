package com.goalias.system.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.goalias.common.core.constant.Constants;
import com.goalias.common.core.domain.model.LoginUser;
import com.goalias.common.core.enums.DeviceType;
import com.goalias.common.core.enums.LoginType;
import com.goalias.common.core.enums.UserStatus;
import com.goalias.common.core.exception.user.CaptchaException;
import com.goalias.common.core.exception.user.CaptchaExpireException;
import com.goalias.common.core.exception.user.UserException;
import com.goalias.common.core.utils.DateUtils;
import com.goalias.common.core.utils.ServletUtils;
import com.goalias.common.core.utils.SpringUtils;
import com.goalias.common.core.utils.StringUtils;
import com.goalias.common.redis.constant.GlobalConstants;
import com.goalias.common.redis.service.RedisService;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.system.domain.event.LogininforEvent;
import com.goalias.system.domain.SysUser;
import com.goalias.system.domain.vo.SysUserVo;
import com.goalias.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 登录校验方法
 *
 * @author Goalias
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysLoginService {

    private final SysUserMapper userMapper;
    private final RedisService redisService;

    @Value("${user.password.maxRetryCount}")
    private Integer maxRetryCount;
    @Value("${user.password.lockTime}")
    private Integer lockTime;

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid) {
        SysUserVo user = loadUserByUsername(username);
        checkLogin(LoginType.PASSWORD, username, () -> !BCrypt.checkpw(password, user.getPassword()));
        // 此处可根据登录用户的数据不同 自行创建 loginUser
        LoginUser loginUser = buildLoginUser(user);
        // 生成token
        LoginHelper.loginByDevice(loginUser, DeviceType.PC);

        recordLogininfor(username, Constants.LOGIN_SUCCESS, "登录成功");
        recordLoginInfo(user.getUserId());
        return StpUtil.getTokenValue();
    }

    /**
     * 退出登录
     */
    public void logout() {
        try {
            StpUtil.logout();
            LoginUser loginUser = LoginHelper.getLoginUser();
            if (loginUser != null) {
                recordLogininfor(loginUser.getUsername(), Constants.LOGOUT, "退出登录");
            }
        } catch (NotLoginException ignored) {
        }
    }

    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status   状态
     * @param message  消息内容
     */
    private void recordLogininfor(String username, String status, String message) {
        LogininforEvent logininforEvent = new LogininforEvent();
        logininforEvent.setUsername(username);
        logininforEvent.setStatus(status);
        logininforEvent.setMessage(message);
        logininforEvent.setRequest(ServletUtils.getRequest());
        SpringUtils.context().publishEvent(logininforEvent);
    }


    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     */
    public void validateCaptcha(String username, String code, String uuid) {
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + StringUtils.defaultString(uuid, "");
        String captcha = redisService.get(verifyKey).toString();
        redisService.del(verifyKey);
        if (captcha == null) {
            recordLogininfor(username, Constants.LOGIN_FAIL, "验证码已过期，请重新获取");
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            recordLogininfor(username, Constants.LOGIN_FAIL, "验证码错误");
            throw new CaptchaException();
        }
    }

    private SysUserVo loadUserByUsername(String username) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().select(SysUser::getUserName, SysUser::getStatus)
                .eq(SysUser::getUserName, username));
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", username);
            throw new UserException("user.not.exists", username);
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new UserException("user.blocked", username);
        }
        return userMapper.selectUserByUserName(username);
    }

    /**
     * 构建登录用户
     */
    private LoginUser buildLoginUser(SysUserVo user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setUsername(user.getUserName());
        loginUser.setNickName(user.getNickName());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setUserType(user.getUserType());
        loginUser.setKroleGroupIds(user.getKroleGroupIds());
        loginUser.setKroleGroupType(user.getKroleGroupType());
        return loginUser;
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(ServletUtils.getClientIP());
        sysUser.setLoginDate(DateUtils.getNowDate());
        sysUser.setUpdateBy(userId);
        userMapper.updateById(sysUser);
    }

    /**
     * 登录校验
     */
    private void checkLogin(LoginType loginType, String username, Supplier<Boolean> supplier) {
        String errorKey = GlobalConstants.PWD_ERR_CNT_KEY + username;
        String loginFail = Constants.LOGIN_FAIL;

        // 获取用户登录错误次数(可自定义限制策略 例如: key + username + ip)
        Integer errorNumber = (Integer) redisService.get(errorKey);
        String retryMsg = String.format("密码尝试次数已超过%d次,已锁定%d分钟", maxRetryCount, lockTime);
        // 锁定时间内登录 则踢出
        if (ObjectUtil.isNotNull(errorNumber) && errorNumber.equals(maxRetryCount)) {
            recordLogininfor(username, loginFail, retryMsg);
            throw new UserException(retryMsg);
        }

        if (supplier.get()) {
            // 是否第一次
            errorNumber = ObjectUtil.isNull(errorNumber) ? 1 : errorNumber + 1;
            // 达到规定错误次数 则锁定登录
            if (errorNumber.equals(maxRetryCount)) {
                redisService.set(errorKey, errorNumber, Duration.ofMinutes(lockTime));
                recordLogininfor(username, loginFail, retryMsg);
                throw new UserException(retryMsg);
            } else {
                // 未达到规定错误次数 则递增
                redisService.set(errorKey, errorNumber, Duration.ofMinutes(lockTime));
                String tip = String.format("密码错误,已输入%d次", errorNumber);
                recordLogininfor(username, loginFail, tip);
                throw new UserException(tip);
            }
        }

        // 登录成功 清空错误次数
        redisService.del(errorKey);
    }

}
