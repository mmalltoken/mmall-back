package com.mmall.util;

import java.math.BigDecimal;

/**
 * 描述：BigDecimal计算工具类
 * 作者：NearJC
 * 时间：2019.4.27
 */
public class BigDecimalUtil {

    public static BigDecimal add(double d1, double d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);

        return b1.add(b2);
    }

    public static BigDecimal sub(double d1, double d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);

        return b1.subtract(b2);
    }

    public static BigDecimal mul(double d1, double d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);

        return b1.multiply(b2);
    }

    public static BigDecimal div(double d1, double d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);

        return b1.divide(b2, BigDecimal.ROUND_HALF_UP);
    }
}
