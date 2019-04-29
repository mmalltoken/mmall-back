package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.*;

@Service("categoryService")
public class CategoryService implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 添加分类
     *
     * @param categoryName
     * @param parentId
     * @return
     */
    public ServerResponse<String> addCategory(String categoryName, Integer parentId) {
        if (parentId == null || StringUtils.isEmpty(categoryName)) {
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }

        Category category = new Category();
        category.setParentId(parentId);
        category.setName(categoryName);
        category.setStatus(true); // 这个分类是可用的

        int rowCount = categoryMapper.insert(category);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }

        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    /**
     * 更新分类名称
     *
     * @param categoryId
     * @param categoryName
     * @return
     */
    public ServerResponse<String> updateCategoryName(Integer categoryId, String categoryName) {

        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("修改品类参数错误");
        }

        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);

        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);

        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("修改品类名字成功");
        }

        return ServerResponse.createByErrorMessage("修改品类名字失败");
    }

    /**
     * 获取子节点信息
     *
     * @param parentId
     * @return
     */
    @Override
    public ServerResponse<List<Category>> getChildrenNode(Integer parentId) {
        if (parentId == null) {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        List<Category> categoryList = categoryMapper.selectChildrenByParentId(parentId);
        if (CollectionUtils.isEmpty(categoryList)) {
            return ServerResponse.createByErrorMessage("当前分类没有子分类");
        }

        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 查询本身及子节点的id
     *
     * @param categoryId
     * @return
     */
    @Override
    public ServerResponse<List<Integer>> getSelfAndChildrenId(Integer categoryId) {
        if (categoryId == null) {
            return ServerResponse.createByErrorMessage("参数错误");
        }

        Set<Category> categorySet = Sets.newHashSet();
        findSelfAndChildren(categoryId, categorySet);

        if (CollectionUtils.isEmpty(categorySet)) {
            return ServerResponse.createByErrorMessage("查询失败");
        }

        List<Integer> categoryIds = Lists.newArrayList();
        for (Category category : categorySet) {
            categoryIds.add(category.getId());
        }

        return ServerResponse.createBySuccess(categoryIds);
    }

    private Set<Category> findSelfAndChildren(Integer categoryId, Set<Category> categorySet) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categorySet.add(category);
        }

        // 查找子节点
        List<Category> categoryList = categoryMapper.selectChildrenByParentId(categoryId);
        if (!CollectionUtils.isEmpty(categoryList)) {
            for (Category categoryItem : categoryList) {
                findSelfAndChildren(categoryItem.getId(), categorySet);
            }
        }

        return categorySet;
    }
}
