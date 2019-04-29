package com.mmall.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 描述：购物车数据模型
 * 作者：NearJC
 * 时间：2019.4.27
 */
@Getter
@Setter
public class CartVo {

    private List<CartProductVo> cartProductVoList;    // 购物车商品列表
    private boolean allChecked;                       // 全选
    private BigDecimal totalPrice;                    // 总价
    private String imageHost;                         // 图片前缀

}
