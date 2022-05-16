package com.example.library.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class R implements Serializable {
    private int code;
    private Object data;
    private String msg;

    public R(int code, Object data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public static R success(Object data) {
        return new R(200, data, "");
    }

    public static R fail(String msg) {
        return new R(500, null, msg);
    }

    public static R fail(String msg,int code) {
        return new R(code, null, msg);
    }

    public static R success() {
        return new R(200, null, "");
    }

    public Object getData() {
        return data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


}
