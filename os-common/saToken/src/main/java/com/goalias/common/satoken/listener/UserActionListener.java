package com.goalias.common.satoken.listener;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.goalias.common.redis.constant.CacheConstants;
import com.goalias.common.core.domain.dto.UserOnlineDTO;
import com.goalias.common.core.domain.model.LoginUser;
import com.goalias.common.core.enums.UserType;
import com.goalias.common.core.utils.ServletUtils;
import com.goalias.common.redis.service.RedisService;
import com.goalias.common.satoken.utils.LoginHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * 用户行为 侦听器的实现
 *
 * @author Goalias
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class UserActionListener implements SaTokenListener {

    private final SaTokenConfig tokenConfig;
    private final RedisService redisService;

    /**
     * 每次登录时触发
     */
    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginModel loginModel) {
        UserType userType = UserType.getUserType(loginId.toString());
        if (userType == UserType.SYS_USER) {
            UserAgent userAgent = UserAgentUtil.parse(ServletUtils.getRequest().getHeader("User-Agent"));
            String ip = ServletUtils.getClientIP();
            LoginUser user = LoginHelper.getLoginUser();
            UserOnlineDTO dto = new UserOnlineDTO();
            dto.setIpaddr(ip);
           // dto.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
            dto.setBrowser(userAgent.getBrowser().getName());
            dto.setOs(userAgent.getOs().getName());
            dto.setLoginTime(System.currentTimeMillis());
            dto.setTokenId(tokenValue);
            dto.setUserName(user.getUsername());
            dto.setDeptName(user.getDeptName());
            if(tokenConfig.getTimeout() == -1) {
                redisService.set(CacheConstants.ONLINE_TOKEN_KEY + tokenValue, dto);
            } else {
                redisService.set(CacheConstants.ONLINE_TOKEN_KEY + tokenValue, dto, tokenConfig.getTimeout() / 1000L);
            }
            log.info("user doLogin, userId:{}, token:{}", loginId, tokenValue);
        } else if (userType == UserType.APP_USER) {
            // app端 自行根据业务编写
        }
    }

    /**
     * 每次注销时触发
     */
    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        redisService.del(CacheConstants.ONLINE_TOKEN_KEY + tokenValue);
        log.info("user doLogout, userId:{}, token:{}", loginId, tokenValue);
    }

    /**
     * 每次被踢下线时触发
     */
    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        redisService.del(CacheConstants.ONLINE_TOKEN_KEY + tokenValue);
        log.info("user doKickout, userId:{}, token:{}", loginId, tokenValue);
    }

    /**
     * 每次被顶下线时触发
     */
    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue) {
        redisService.del(CacheConstants.ONLINE_TOKEN_KEY + tokenValue);
        log.info("user doReplaced, userId:{}, token:{}", loginId, tokenValue);
    }

    /**
     * 每次被封禁时触发
     */
    @Override
    public void doDisable(String loginType, Object loginId, String service, int level, long disableTime) {
    }

    /**
     * 每次被解封时触发
     */
    @Override
    public void doUntieDisable(String loginType, Object loginId, String service) {
    }

    /**
     * 每次打开二级认证时触发
     */
    @Override
    public void doOpenSafe(String loginType, String tokenValue, String service, long safeTime) {
    }

    /**
     * 每次创建Session时触发
     */
    @Override
    public void doCloseSafe(String loginType, String tokenValue, String service) {
    }

    /**
     * 每次创建Session时触发
     */
    @Override
    public void doCreateSession(String id) {
    }

    /**
     * 每次注销Session时触发
     */
    @Override
    public void doLogoutSession(String id) {
    }

    /**
     * 每次Token续期时触发
     */
    @Override
    public void doRenewTimeout(String tokenValue, Object loginId, long timeout) {
    }
}
