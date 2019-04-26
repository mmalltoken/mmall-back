package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Product;
import com.mmall.service.IProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("productService")
public class ProductService implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            // 检测商品分类是否为空
            if (product.getCategoryId() == null) {
                return ServerResponse.createByErrorMessage("商品分类不允许为空");
            }

            // 检测商品名称是否为空
            if (StringUtils.isBlank(product.getName())) {
                return ServerResponse.createByErrorMessage("商品名称不允许为空");
            }

            // 检测商品价格是否为空
            if (product.getPrice() == null) {
                return ServerResponse.createByErrorMessage("商品价格不允许为空");
            }

            // 检测库存是否为空
            if (product.getStock() == null) {
                return ServerResponse.createByErrorMessage("商品库存不允许为空");
            }

            // 判断是否有子图
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImageArray = product.getSubImages().split(",");
                if (subImageArray.length > 0) {
                    product.setMainImage(subImageArray[0]);
                }
            }

            if (product.getId() != null) { // 更新产品
                int rowCount = productMapper.updateByPrimaryKey(product);
                if (rowCount > 0) {
                    return ServerResponse.createBySuccess("更新商品成功");
                }
                return ServerResponse.createBySuccess("更新商品失败");
            } else { // 添加产品
                int rowCount = productMapper.insert(product);
                if (rowCount > 0) {
                    return ServerResponse.createBySuccess("新增商品成功");
                }
                return ServerResponse.createBySuccess("新增商品失败");
            }
        }

        return ServerResponse.createByErrorMessage("新增或更新商品参数不正确");
    }
}
