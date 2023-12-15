package com.shark.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shark.usercenter.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.shark.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.shark.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
* @author admin_oyh
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2023-11-11 22:36:42
*/
public interface UserService extends IService<User> {

    /**
     *
     * @param userAccount 用户账户
     * @param userPassword 密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     *
     * @param userAccount
     * @param userPassword
     * @return
     */
    User doLogin(String userAccount, String userPassword, HttpServletRequest httpServletRequest);

    User getSafetyUser(User originUser);
    /**
     * 退出登录
     */
    void userLogout(HttpServletRequest httpServletRequest);

    /**
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    Integer updateUser(User updateUser, HttpServletRequest request);
    boolean isAdmin(HttpServletRequest httpServletRequest);
    User getLoginUser(HttpServletRequest request);
}
