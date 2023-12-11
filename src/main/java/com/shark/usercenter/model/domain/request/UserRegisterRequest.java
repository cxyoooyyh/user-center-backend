package com.shark.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sharkCode
 * @date 2023/11/17 16:05
 * 用户注册请求体
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 6441736032589365375L;
    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;
}
