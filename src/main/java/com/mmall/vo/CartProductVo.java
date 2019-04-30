package com.mmall.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 描述：购物车中的商品信息
 * 作者：NearJC
 * 时间：2019.4.27
 */
@Getter
@Setter
public class CartProductVo {

    private Integer id;                     // 购物车id
    private Integer userId;                 // 用户id
    private Integer productId;              // 商品id
    private Integer quantity;               // 购买数量
    private String productName;             // 商品名称
    private String productSubtitle;         // 商品标题
    private String productMainImage;        // 商品图片
    private BigDecimal productPrice;        // 商品价格
    private Integer productStock;           // 商品库存
    private Integer productStatus;          // 商品状态
    private Integer productChecked;         // 勾选
    private String limitQuantity;           // 限制购买数量
    private BigDecimal productTotalPrice;   // 购买商品总价格
}
