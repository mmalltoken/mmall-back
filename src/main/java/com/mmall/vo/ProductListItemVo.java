package com.mmall.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 描述：商品列表数据项
 * 作者：NearJC
 * 时间：2019.4.26
 */
@Getter
@Setter
public class ProductListItemVo {

    private Integer id;
    private String name;
    private String subtitle;
    private String mainImage;
    private BigDecimal price;
    private Integer status;
    private Integer stock;
    private String imageHost;

}
