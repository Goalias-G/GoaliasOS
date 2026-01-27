package com.goalias.chat.chat.tools;

import cn.dev33.satoken.stp.StpUtil;
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

    @OsTool(name = "getWeather", description = "根据城市名称查询当前天气")
    public String getWeather(
            @OsToolParam(name = "city", description = "查询的城市名称，例如 '苏州'") String city,
            @OsToolParam(name = "unit", description = "温度单位", required = false) String unit
    ) {
        return city + "天气：晴，温度：25摄氏度";
    }

    @OsTool(name = "getUserInfo", description = "获取用户的信息，不传参数则是当前登录用户")
    public SysUserVo getUserInfo(@OsToolParam(name = "userId", description = "用户ID") Long userId) {
        if (Objects.isNull(userId)) {
            LoginUser loginUser = LoginHelper.getLoginUser(BaseContext.getCurrentToken());
            return BeanUtil.copyProperties(loginUser, SysUserVo.class);
        }
        return userService.selectUserById(userId);
    }
}
