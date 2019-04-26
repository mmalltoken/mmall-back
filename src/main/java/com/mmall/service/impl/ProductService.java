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

    /**
     * 更新商品销售状态
     *
     * @param productId
     * @param status
     * @return
     */
    @Override
    public ServerResponse<String> setSaleStatus(Integer productId, Integer status) {
        // 参数校验
        if (productId == null || status == null) {
            return ServerResponse.createByErrorMessage("更新商品销售状态参数不正确");
        }

        // 封装商品数据
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);

        // 更新状态
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("修改产品销售状态成功");
        }

        return ServerResponse.createByErrorMessage("修改产品销售状态失败");
    }
}
