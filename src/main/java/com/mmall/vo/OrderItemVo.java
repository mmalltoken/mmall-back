package com.mmall.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 描述：订单明细Vo数据模型类
 * 作者：NearJC
 * 时间：2019.4.28
 */
@Getter
@Setter
public class OrderItemVo {

    private Long orderNo;
    private Integer productId;
    private String productName;
    private String productImage;
    private BigDecimal currentUnitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String createTime;

}
