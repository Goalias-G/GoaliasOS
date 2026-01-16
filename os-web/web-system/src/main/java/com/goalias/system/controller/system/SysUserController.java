package com.goalias.system.controller.system;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.goalias.common.core.domain.R;
import com.goalias.common.core.domain.model.LoginUser;
import com.goalias.common.core.utils.StringUtils;
import com.goalias.common.satoken.utils.LoginHelper;
import com.goalias.common.web.core.BaseController;
import com.goalias.common.web.domain.PageQuery;
import com.goalias.common.web.domain.TableDataInfo;
import com.goalias.system.domain.SysOss;
import com.goalias.system.domain.bo.SysUserBo;
import com.goalias.system.domain.request.UserRequest;
import com.goalias.system.domain.vo.SysUserInfoVo;
import com.goalias.system.domain.vo.SysUserOptionVo;
import com.goalias.system.domain.vo.SysUserVo;
import com.goalias.system.domain.vo.UserInfoVo;
import com.goalias.system.service.ISysOssService;
import com.goalias.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

;

/**
 * 用户信息
 *
 * @author Goalias
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {

    private final ISysUserService userService;
    private final ISysOssService ossService;
    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public TableDataInfo<SysUserVo> list(SysUserBo user, PageQuery pageQuery) {
        return userService.selectPageUserList(user, pageQuery);
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/getUserOption")
    public R<List<SysUserOptionVo>> getUserOption() {
        List<SysUserVo> sysUserVos = userService.selectUserList(new SysUserBo());
        List<SysUserOptionVo> collect = sysUserVos.stream()
            .map(this::convertToUserOptionVo)
            .collect(Collectors.toList());
        return R.ok(collect);
    }

    private SysUserOptionVo convertToUserOptionVo(SysUserVo sysUserVo) {
        SysUserOptionVo sysUserOptionVo = new SysUserOptionVo();
        sysUserOptionVo.setUserId(sysUserVo.getUserId());
        sysUserOptionVo.setName(sysUserVo.getNickName());
        return sysUserOptionVo;
    }


    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/getInfo")
    public R<UserInfoVo> getInfo() {
        UserInfoVo userInfoVo = new UserInfoVo();
        LoginUser loginUser = LoginHelper.getLoginUser();
        SysUserVo user = userService.selectUserById(loginUser.getUserId());
        userInfoVo.setUser(user);
        return R.ok(userInfoVo);
    }

    /**
     * 根据用户编号获取详细信息
     *
     * @param userId 用户ID
     */
    @GetMapping(value = {"/", "/{userId}"})
    public R<SysUserInfoVo> getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        userService.checkUserDataScope(userId);
        SysUserInfoVo userInfoVo = new SysUserInfoVo();
        if (ObjectUtil.isNotNull(userId)) {
            SysUserVo sysUser = userService.selectUserById(userId);
            userInfoVo.setUser(sysUser);
        }
        return R.ok(userInfoVo);
    }

    /**
     * 修改用户
     */
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysUserBo user) {
        userService.checkUserAllowed(user.getUserId());
        userService.checkUserDataScope(user.getUserId());
        if (!userService.checkUserNameUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        return toAjax(userService.updateUser(user));
    }

    /**
     * 修改用户名称
     */
    @PostMapping("/editName")
    public R<Void> editName(@RequestBody @Validated UserRequest userRequest) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        userService.updateUserName(loginUser.getUserId(), userRequest.getNickName());
        return R.ok("操作成功!");
    }

    /**
     * 修改用户头像
     */
    @PostMapping("/edit/avatar")
    public R<Void> editAvatar(@RequestPart("file") MultipartFile file) {
        if (ObjectUtil.isNull(file)) {
            return R.fail("上传文件不能为空");
        }
        LoginUser loginUser = LoginHelper.getLoginUser();
        // 获取当前登录用户
        SysOss oss = ossService.upload(file);
        userService.updateUserAvatar(loginUser.getUserId(), oss.getUrl());
        return R.ok(oss.getUrl());
    }

    /**
     * 删除用户
     *
     * @param userIds 角色ID串
     */
    @DeleteMapping("/{userIds}")
    public R<Void> remove(@PathVariable Long[] userIds) {
        if (ArrayUtil.contains(userIds, LoginHelper.getUserId())) {
            return R.fail("当前用户不能删除");
        }
        return toAjax(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
    @PutMapping("/resetPwd")
    public R<Void> resetPwd(@RequestBody SysUserBo user) {
        userService.checkUserAllowed(user.getUserId());
        userService.checkUserDataScope(user.getUserId());
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        return toAjax(userService.resetUserPwd(user.getUserId(), user.getPassword()));
    }

    /**
     * 状态修改
     */
    @PutMapping("/changeStatus")
    public R<Void> changeStatus(@RequestBody SysUserBo user) {
        userService.checkUserAllowed(user.getUserId());
        userService.checkUserDataScope(user.getUserId());
        return toAjax(userService.updateUserStatus(user.getUserId(), user.getStatus()));
    }

}
