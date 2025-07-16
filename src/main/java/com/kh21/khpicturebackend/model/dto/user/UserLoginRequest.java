package com.kh21.khpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户电路请求
 */
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 7531797998586912929L;
    /**
     * 账号
     */
    private String userAccount;
    /**
     * 密码
     */
    private String userPassword;
}
