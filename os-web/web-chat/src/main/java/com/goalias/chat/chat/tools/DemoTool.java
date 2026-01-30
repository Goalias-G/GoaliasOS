package com.goalias.chat.chat.tools;

import cn.hutool.core.bean.BeanUtil;
import com.goalias.chat.chat.support.BaseContext;
import com.goalias.common.core.domain.model.LoginUser;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.system.domain.vo.SysUserVo;
import com.goalias.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DemoTool implements OsToolProvider {

    private final ISysUserService userService;

    @OsTool(name = "getUserInfo", description = "获取用户的信息，不传参数则是当前登录用户")
    public SysUserVo getUserInfo(@OsToolParam(name = "userId", description = "用户ID") Long userId) {
        if (Objects.isNull(userId)) {
            LoginUser loginUser = LoginHelper.getLoginUser(BaseContext.getCurrentToken());
            return BeanUtil.copyProperties(loginUser, SysUserVo.class);
        }
        return userService.selectUserById(userId);
    }
}
