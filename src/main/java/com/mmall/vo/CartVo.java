package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 描述：购物车数据模型
 * 作者：NearJC
 * 时间：2019.4.27
 */
public class CartVo {

    private List<CartProductVo> cartProductVoList;    // 购物车商品列表
    private boolean allChecked;                       // 全选
    private BigDecimal totalPrice;                    // 总价
    private String imageHost;                         // 图片前缀

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }

    public boolean isAllChecked() {
        return allChecked;
    }

    public void setAllChecked(boolean allChecked) {
        this.allChecked = allChecked;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
