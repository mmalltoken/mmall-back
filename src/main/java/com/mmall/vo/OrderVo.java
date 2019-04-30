package com.mmall.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 描述：订单Vo数据模型类
 * 作者：NearJC
 * 时间：2019.4.28
 */
@Getter
@Setter
public class OrderVo {

    private Long orderNo;
    private BigDecimal payment;
    private Integer paymentType;
    private String paymentTypeDesc;
    private Integer postage;
    private Integer status;
    private String statusDesc;
    private String paymentTime;
    private String sendTime;
    private String endTime;
    private String closeTime;
    private String createTime;
    private Integer shippingId;
    private String receiveName;
    private List<OrderItemVo> orderItemVoList;
    private ShippingVo shippingVo;
    private String imageHost;

}
