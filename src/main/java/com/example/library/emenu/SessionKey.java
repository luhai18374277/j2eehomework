package com.example.library.emenu;

import lombok.Getter;
import lombok.Setter;

public enum SessionKey {
    MANANGER_SESSION_key("lginManager","管理员登录信息"),
    USER_SESSION_key("lginUser","用户登录信息");
    @Getter
    @Setter
    private String code;
    SessionKey(String code, String desc) {
        this.code=code;
    }

    public static SessionKey getValue(String code){
        for (SessionKey value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
