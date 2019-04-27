package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListItemVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("productService")
public class ProductService implements IProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private CategoryService categoryService;

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

    @Override
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        if (productId == null) {
            return ServerResponse.createByErrorMessage("查询商品参数不正确");
        }

        // 根据商品id查询信息
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("查询失败，商品不存在");
        }

        // 封装数据
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);

        return ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     * 查询商品信息
     *
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> backendSearchProduct(String productName, Integer productId, int pageNum, int pageSize) {
        // 拼接商品名称条件
        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }

        // 分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.selectByIdAndName(productId, productName);

        // 封装数据
        List<ProductListItemVo> productListItemVoList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(productList)) {
            for (Product product : productList) {
                ProductListItemVo productListItemVo = assembleProductListItemVo(product);
                productListItemVoList.add(productListItemVo);
            }
        }

        // 封装分页信息
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListItemVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 获取商品信息
     *
     * @param productId
     * @return
     */
    @Override
    public ServerResponse<ProductDetailVo> productDetail(Integer productId) {
        // 参数校验
        if (productId == null) {
            return ServerResponse.createByErrorMessage("查询商品详情参数不正确");
        }

        // 查询
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null || product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createByErrorMessage("产品不存在或已下架");
        }

        ProductDetailVo productDetailVo = assembleProductDetailVo(product);

        return ServerResponse.createBySuccess(productDetailVo);
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
    @Override
    public ServerResponse portalSearchProduct(String keyword, Integer categoryId, String orderBy, int pageNum, int pageSize) {
        // 参数校验
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.createByErrorMessage("查询条件参数错误");
        }

        // 查询分类的子分类
        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId != null) {
            // 没有该分类，并且还没有搜索关键字，这个时间返回一个空结果集，不报错
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) {
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListItemVo> productListItemVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListItemVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }

            ServerResponse<List<Integer>> response = categoryService.getSelfAndChildrenId(categoryId);
            if (response.isSuccess()) {
                categoryIdList = response.getData();
            }
        }

        // 关键词拼接
        if (StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        // 处理排序
        if (StringUtils.isNotBlank(orderBy)) {
            String[] orderByArray = orderBy.split("_");
            PageHelper.orderBy(orderByArray[0] + " " + orderByArray[1]);
        }

        // 分页查询
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.selectByKeywordAndCategoryIds(keyword, categoryIdList);

        // 封闭响应数据
        List<ProductListItemVo> productListItemVoList = Lists.newArrayList();
        for (Product product : productList) {
            ProductListItemVo productListItemVo = assembleProductListItemVo(product);
            productListItemVoList.add(productListItemVo);
        }
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListItemVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 封装商品详情
     *
     * @param product
     * @return
     */
    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();

        productDetailVo.setId(product.getId());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setName(product.getName());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://img.lzy.com/"));
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null) {
            productDetailVo.setParentCategoryId(0);   // 默认根节点
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }

        return productDetailVo;
    }

    /**
     * 封装商品列表项
     *
     * @param product
     * @return
     */
    private ProductListItemVo assembleProductListItemVo(Product product) {
        ProductListItemVo productListItemVo = new ProductListItemVo();
        productListItemVo.setId(product.getId());
        productListItemVo.setName(product.getName());
        productListItemVo.setSubtitle(product.getSubtitle());
        productListItemVo.setMainImage(product.getMainImage());
        productListItemVo.setPrice(product.getPrice());
        productListItemVo.setStock(product.getStock());
        productListItemVo.setStatus(product.getStatus());
        productListItemVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "ftp://img.lzy.com/"));

        return productListItemVo;
    }
}
