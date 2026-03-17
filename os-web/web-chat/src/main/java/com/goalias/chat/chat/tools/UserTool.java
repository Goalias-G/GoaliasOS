package com.goalias.chat.chat.tools;

import com.goalias.chat.chat.event.UserContextUpdateEvent;
import com.goalias.chat.chat.support.TtlTokenContext;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.system.domain.vo.SysUserVo;
import com.goalias.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserTool implements OsToolProvider {

    private final ISysUserService userService;

    private final ApplicationEventPublisher eventPublisher;

    @OsTool(name = "get_user_info", description = "获取用户的信息，不传参数则是当前登录用户")
    public SysUserVo getUserInfo(@OsToolParam(name = "userId", description = "用户ID") Long userId) {
        Long ttlUserId = TtlTokenContext.getCurrentUserId();
        log.debug("getUserInfo 执行 userId: {}", userId);

        Long id = Optional.ofNullable(userId).orElse(ttlUserId);
        if (!LoginHelper.isSuperAdmin(ttlUserId) && !ttlUserId.equals(id)) {
            return null;
        }
        SysUserVo sysUserVo = userService.selectUserById(id);
        sysUserVo.setIsAdmin(LoginHelper.isSuperAdmin(id));
        return sysUserVo;
    }

    @OsTool(name = "update_user_context", description = "当对话中存在此用户对自己个人背景身份信息有描述时调用")
    public Boolean updateUserContext(@OsToolParam(name = "description", description = "包含用户个人描述的原始描述片段", required = true) String description) {
        Long ttlUserId = TtlTokenContext.getCurrentUserId();
        log.debug("updateUserContext 执行 userId: {}", ttlUserId);

        eventPublisher.publishEvent(new UserContextUpdateEvent(ttlUserId, description));
        return true;
    }
}
