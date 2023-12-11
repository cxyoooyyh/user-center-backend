package com.shark.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * @author sharkCode
 * @date 2023/11/30 9:07
 */
@Data
public class BaseResponse<T>  implements Serializable {
    private T data;
    private int code;
    private String message;
    private String description;

    public BaseResponse(T data, int code, String message, String description) {
        this.data = data;
        this.code = code;
        this.message = message;
        this.description = description;
    }
    public BaseResponse(T data, int code, String message) {
        this(data, code, message, "");
    }
    public BaseResponse(T data, int code) {
        this(data, code, "", "");
    }
    public BaseResponse(ErrorCode errorCode) {
        this(null, errorCode.getCode(), errorCode.getMessage(), errorCode.getDescription());
    }
    public BaseResponse(ErrorCode errorCode, String message, String description) {
        this(null, errorCode.getCode(), message, description);
    }
}
