package com.guo.core.common.constant;

/**
 * Created by guo on 3/2/2018.
 * 从SpringApplicationContext中设置的系统参数
 */
public class SystemConfig {

    //系统默认的游客用户名
    private static String guestUsername = "";

    private SystemConfig() {

    }

    public static String getGuestUsername() {
        return guestUsername;
    }

    public static void setGuestUsername(String guestUsername) {
        SystemConfig.guestUsername = guestUsername;
    }
}
