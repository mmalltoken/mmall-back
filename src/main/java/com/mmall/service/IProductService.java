package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

public interface IProductService {

    ServerResponse<String> saveOrUpdateProduct(Product product);

    ServerResponse<String> setSaleStatus(Integer productId, Integer status);

    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);

    ServerResponse<PageInfo> backendSearchProduct(String productName, Integer productId, int pageNum, int pageSize);

    // --------------前台接口--------------------
    
    ServerResponse<ProductDetailVo> productDetail(Integer productId);

    ServerResponse portalSearchProduct(String keyword, Integer categoryId, String orderBy, int pageNum, int pageSize);
}
