package com.mmall.controller.portal;

import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 描述：前台 - 商品模块
 * 作者：NearJC
 * 时间：2019.4.27
 */
@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService productService;

    /**
     * 查询商品详情
     *
     * @param productId
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId) {
        return productService.productDetail(productId);
    }
}
