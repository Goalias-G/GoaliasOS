package com.goalias.system.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户信息
 *
 * @author Goalias
 */
@Data
public class SysUserInfoVo {

    /**
     * 用户信息
     */
    private SysUserVo user;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;

    /**
     * 岗位ID列表
     */
    private List<Long> postIds;

}
