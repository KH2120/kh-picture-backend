package com.kh21.khpicturebackend.model.dto.user;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


@Data
public class UserUpdateRequest implements Serializable {
    private static final long serialVersionUID = -8643984515882426701L;
    /**
     * id
     */
    private Long id;
    /**
     * 用户昵称
     */
    private String userName;
    /**
     * 账户
     */
    private String userAccount;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色
     */
    private String userRole;

}
