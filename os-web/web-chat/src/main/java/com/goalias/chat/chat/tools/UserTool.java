package com.goalias.chat.chat.tools;

import com.goalias.chat.chat.support.TtlTokenContext;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.system.domain.vo.SysUserVo;
import com.goalias.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserTool implements OsToolProvider {

    private final ISysUserService userService;

    @OsTool(name = "get_user_info", description = "获取用户的信息，不传参数则是当前登录用户")
    public SysUserVo getUserInfo(@OsToolParam(name = "userId", description = "用户ID") Long userId) {
        Long ttlUserId = TtlTokenContext.getCurrentUserId();
        Long id = Optional.ofNullable(userId).orElse(ttlUserId);
        if (!LoginHelper.isSuperAdmin(ttlUserId) && !ttlUserId.equals(id)){
            return null;
        }
        SysUserVo sysUserVo = userService.selectUserById(id);
        sysUserVo.setIsAdmin(LoginHelper.isSuperAdmin(id));
        return sysUserVo;
    }
}
