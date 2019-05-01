package com.mmall.dao;

import com.mmall.pojo.Order;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    Order selectByUserIdAndOrderNo(@Param("userId") Integer userId, @Param("orderNo") Long orderNo);

    List<Order> selectByUserId(Integer userId);

    Order selectByOrderNo(Long orderNo);

    List<Order> selectByCondition(@Param("orderNo") Long orderNo);

    List<Order> selectByStatusAndCreateTime(@Param("status") int status, @Param("date") Date closeDate);

    int closeOrderByPrimaryKey(Integer id);
}