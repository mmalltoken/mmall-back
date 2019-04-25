package com.mmall.common;

/**
 * 描述：全局常量封装类
 * 作者：NearJC
 * 时间：2019.4.25
 */
public class Const {

    public static final String CURRENT_USER = "currentUser";

    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    // 角色类型
    public interface Role {
        int ROLE_CUSTOMER = 0; // 普通用户
        int ROLE_ADMIN = 1;  // 管理员
    }
}