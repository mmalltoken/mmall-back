package com.mmall.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 描述：购物车勾选的商品列表
 * 作者：NearJC
 * 时间：2019.4.27
 */
@Getter
@Setter
public class CartCheckedProductVo {

    List<OrderItemVo> orderItemVoList;
    BigDecimal payment;
    String imageHost;
}
