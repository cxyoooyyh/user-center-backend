package com.shark.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sharkCode
 * @date 2023/11/17 16:05
 * 用户注册请求体
 */
@Data
public class UserLoginRequest implements Serializable {


    private static final long serialVersionUID = 7984523998502142416L;
    private String userAccount;
    private String userPassword;
}
