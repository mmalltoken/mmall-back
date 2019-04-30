package com.mmall.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 描述：收货地址信息模块
 * 作者：NearJC
 * 时间：2019.4.26
 */
@Getter
@Setter
public class ShippingVo {

    private String receiverName;
    private String receiverPhone;
    private String receiverMobile;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverAddress;
    private String receiverZip;

}
