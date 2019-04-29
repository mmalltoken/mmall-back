package com.mmall.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 描述：商品详情Vo模型
 * 作者：NearJC
 * 时间：2019.4.26
 */
@Getter
@Setter
public class ProductDetailVo {

    private Integer id;
    private Integer categoryId;
    private String name;
    private String subtitle;
    private String mainImage;
    private String subImages;
    private String detail;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private String createTime;
    private String updateTime;
    private String imageHost;
    private Integer parentCategoryId;

}
