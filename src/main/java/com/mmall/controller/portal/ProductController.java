package com.mmall.controller.portal;

import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
    @RequestMapping(value = "detail.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId) {
        return productService.productDetail(productId);
    }

    /**
     * 条件查询商品
     *
     * @param keyword
     * @param categoryId
     * @param orderBy
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "search.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse search(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "orderBy", defaultValue = "price_desc") String orderBy,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {

        return productService.portalSearchProduct(keyword, categoryId, orderBy, pageNum, pageSize);
    }

}
