package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 描述：购物车勾选的商品列表
 * 作者：NearJC
 * 时间：2019.4.27
 */
public class CartCheckedProductVo {

    List<OrderItemVo> orderItemVoList;
    BigDecimal payment;
    String imageHost;

    public List<OrderItemVo> getOrderItemVoList() {
        return orderItemVoList;
    }

    public void setOrderItemVoList(List<OrderItemVo> orderItemVoList) {
        this.orderItemVoList = orderItemVoList;
    }

    public BigDecimal getPayment() {
        return payment;
    }

    public void setPayment(BigDecimal payment) {
        this.payment = payment;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
