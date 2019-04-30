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

    // 订单状态
    public enum OrderStatusEnum {
        CANCELED(0, "已取消"),
        NO_PAY(10, "未支付"),
        PAID(20, "已支付"),
        SHIPPED(40, "已发货"),
        ORDER_SUCCESS(50, "订单完成"),
        ORDER_CLOSE(60, "订单关闭");

        private int code;
        private String desc;

        OrderStatusEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static OrderStatusEnum codeOf(Integer code) {
            for (OrderStatusEnum orderStatusEnum : values()) {
                if (orderStatusEnum.getCode() == code) {
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的类型");
        }
    }

    // 支付类型
    public enum PaymentTypeEnum {
        ONLINE_PAY(1, "在线支付");

        private int code;
        private String desc;

        PaymentTypeEnum(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static PaymentTypeEnum codeOf(Integer code) {
            for (PaymentTypeEnum paymentTypeEnum : values()) {
                if (paymentTypeEnum.getCode() == code) {
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到对应的类型");
        }

    }

    // 支付宝回调响应状态
    public interface AlipayCallback {
        String TRADE_SUCCESS = "TRADE_SUCCESS";
        String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_CLOSED = "TRADE_CLOSED";
        String TRADE_FINISHED = "TRADE_FINISHED";
    }

    // 购物车
    public interface Cart {
        int CHECKED = 1;     // 商品勾选
        int UN_CHECKED = 0;  // 商品未勾选

        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";   // 限制成功
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";         // 限制失败
    }

    // 支付平台类型
    public enum PayPlatformEnum {

        ALIPAY(1, "支付宝"),
        WECHAT(2, "微信");

        private int code;
        private String desc;

        PayPlatformEnum(int code, String desc) {
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

    // redis存储
    public interface RedisCache {
        // 用户信息有效时间
        int REDIS_SESSION_EXPIRE = 60 * 60 * 24 * 7;
        // 忘记密码token前缀
        String TOKEN_PREFIX = "token_";
    }
}