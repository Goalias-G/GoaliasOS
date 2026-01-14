package com.goalias.system.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.goalias.system.domain.SysUser;
import com.goalias.system.domain.vo.SysUserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户表 数据层
 *
 * @author Goalias
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    Page<SysUserVo> selectPageUserList(@Param("page") Page<SysUser> page, @Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);


    List<SysUserVo> selectUserList(@Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);


    Page<SysUserVo> selectAllocatedList(@Param("page") Page<SysUser> page, @Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);


    Page<SysUserVo> selectUnallocatedList(@Param("page") Page<SysUser> page, @Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    SysUserVo selectUserByUserName(String userName);

    /**
     * 通过OpenId查询用户
     *
     * @param OpenId 微信用户唯一标识
     * @return 用户对象信息
     */
    SysUserVo selectUserByOpenId(String OpenId);

    /**
     * 通过手机号查询用户
     *
     * @param phonenumber 手机号
     * @return 用户对象信息
     */
    SysUserVo selectUserByPhonenumber(String phonenumber);

    /**
     * 通过邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户对象信息
     */
    SysUserVo selectUserByEmail(String email);

    /**
     * 通过用户名查询用户(不走租户插件)
     *
     * @param userName 用户名
     * @param tenantId 租户id
     * @return 用户对象信息
     */
    @InterceptorIgnore(tenantLine = "true")
    SysUserVo selectTenantUserByUserName(String userName, String tenantId);

    /**
     * 通过手机号查询用户(不走租户插件)
     *
     * @param phonenumber 手机号
     * @param tenantId    租户id
     * @return 用户对象信息
     */
    @InterceptorIgnore(tenantLine = "true")
    SysUserVo selectTenantUserByPhonenumber(String phonenumber, String tenantId);

    /**
     * 通过邮箱查询用户(不走租户插件)
     *
     * @param email    邮箱
     * @param tenantId 租户id
     * @return 用户对象信息
     */
    @InterceptorIgnore(tenantLine = "true")
    SysUserVo selectTenantUserByEmail(String email, String tenantId);


    @InterceptorIgnore(dataPermission = "true")
    SysUserVo selectUserById(Long userId);

    @Override
    @InterceptorIgnore(dataPermission = "true")
    int update(@Param(Constants.ENTITY) SysUser user, @Param(Constants.WRAPPER) Wrapper<SysUser> updateWrapper);

    @Override
    @InterceptorIgnore(dataPermission = "true")
    int updateById(@Param(Constants.ENTITY) SysUser user);
}
