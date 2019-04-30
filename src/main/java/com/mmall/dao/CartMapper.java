package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectByProductIdAndUserId(@Param("productId") Integer productId, @Param("userId") Integer userId);

    List<Cart> selectByUserId(Integer userId);

    int selectCheckedAll(Integer userId);

    int deleteByProductIdsAndUserId(@Param("productIdList") List<String> productIdList, @Param("userId") Integer userId);

    int updateCartProductChecked(@Param("userId") Integer userId, @Param("productId") Integer productId, @Param("checked") int checked);

    int selectCartProductCount(Integer userId);

    List<Cart> selectCheckedProductByUserId(Integer userId);
}