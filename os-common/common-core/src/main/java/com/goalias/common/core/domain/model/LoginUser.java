package com.goalias.common.core.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.goalias.common.core.domain.dto.RoleDTO;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 登录用户身份权限
 *
 * @author Goalias
 */

@Data
@NoArgsConstructor
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;


    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 用户套餐
     */
    private String userPlan;

    /**
     * 登录IP地址
     */
    private String loginIp;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date loginDate;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户名
     */
    private String nickName;

    /**
     * 微信头像
     */
    private String avatar;

    /**
     * 获取登录id
     */
    public String getLoginId() {
        if (Objects.isNull(userId)) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return String.valueOf(userId);
    }

}
