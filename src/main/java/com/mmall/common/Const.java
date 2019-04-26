package com.mmall.common;

import com.mmall.pojo.Product;

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

    // 商品状态
    public enum ProductStatusEnum {
        ON_SALE(1, "在线");

        private int code;
        private String desc;

        ProductStatusEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}