package com.kh21.khpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kh21.khpicturebackend.model.dto.user.UserQueryRequest;
import com.kh21.khpicturebackend.model.entity.User;
import com.kh21.khpicturebackend.model.vo.LoginUserVO;
import com.kh21.khpicturebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author KH2120
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-07-16 14:21:07
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  密码
     * @param checkPassword 确认密码
     * @return long 新用户id
     */

    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 密码
     * @return 脱敏后的用户信息
     */

    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 密码加密
     *
     * @param userPasswrod 用户密码
     * @return String 加密后的密码
     */
    String getEncryptPassword(String userPasswrod);

    /**
     * 获取脱敏后的已登录的user对象
     *
     * @param user
     * @return 脱敏后的已登录的user对象
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏后的user对象
     *
     * @param user 用户
     * @return 脱敏后的user对象
     */
    UserVO getUserVo(User user);

    /**
     * 获取脱敏后的user对象列表
     *
     * @param users 用户列表
     * @return 脱敏后的user对象列表
     */
    List<UserVO> getUserVoList(List<User> users);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return User
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return User
     */
    Boolean userLogout(HttpServletRequest request);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
}
