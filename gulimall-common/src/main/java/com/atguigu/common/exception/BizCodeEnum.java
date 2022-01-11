package com.atguigu.common.exception;

/**
 * @author Yuan Diao
 * @date 2022/1/11
 */

public enum BizCodeEnum {

    VAILD_EXCEPTION(10001, "参数格式校验失败");

    private int code;
    private String message;

    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public int getCode(){
        return code;
    }

    public String getMessage(){
        return message;
    }


}
