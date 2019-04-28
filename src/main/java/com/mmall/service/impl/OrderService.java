package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartCheckedProductVo;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

/**
 * 描述：订单业务实现类
 * 作者：NearJC
 * 时间：2019.4.28
 */
@Service("orderService")
public class OrderService implements IOrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private ShippingMapper shippingMapper;

    /**
     * 创建订单
     *
     * @param shippingId
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<OrderVo> createOrder(Integer shippingId, Integer userId) {
        // 参数校验
        if (shippingId == null) {
            return ServerResponse.createByErrorMessage("创建订单参数错误");
        }

        // 获取购物车中已勾选的商品信息
        List<Cart> cartList = cartMapper.selectCheckedProductByUserId(userId);

        // 封装订单明细
        ServerResponse response = assembleOrderItem(cartList);
        if (!response.isSuccess()) {
            return response;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();

        // 计算支付总价
        BigDecimal payment = calculatePayment(orderItemList);

        // 生成订单
        Order order = assembleOrder(userId, shippingId, payment);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单生成失败");
        }

        // 为订单明细添加订单号
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }

        // 批量插入订单明细
        orderItemMapper.batchInsert(orderItemList);

        // 减少商品库存数量
        reduceProductStock(orderItemList);

        // 清空购物车
        clearCheckedCart(cartList);

        // 封装前端响应数据
        OrderVo orderVo = assembleOrderVo(order, orderItemList);

        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 取消未支付的订单
     *
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse<String> cancelOrder(Long orderNo, Integer userId) {
        if (orderNo == null) {
            return ServerResponse.createByErrorMessage("取消订单参数错误");
        }

        // 查询订单
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }

        // 校验订单状态是否已支付
        if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()) {
            return ServerResponse.createByErrorMessage("已付款，无法取消订单");
        }

        // 更新订单状态为取消
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());
        int rowCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("订单取消成功");
        }

        return ServerResponse.createByErrorMessage("订单取消失败");
    }

    /**
     * 获取购物车中已勾选的商品信息
     *
     * @param userId
     * @return
     */
    @Override
    public ServerResponse getCartCheckedProduct(Integer userId) {
        // 查询购物车中已勾选的商品
        List<Cart> cartList = cartMapper.selectCheckedProductByUserId(userId);
        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }

        // 获取订单明细
        ServerResponse response = assembleOrderItem(cartList);
        if (!response.isSuccess()) {
            return response;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) response.getData();
        // 封装订单明细Vo
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        // 计算支付总金额
        BigDecimal payment = calculatePayment(orderItemList);

        // 填充数据
        CartCheckedProductVo cartCheckedProductVo = new CartCheckedProductVo();
        cartCheckedProductVo.setPayment(payment);
        cartCheckedProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        cartCheckedProductVo.setOrderItemVoList(orderItemVoList);

        return ServerResponse.createBySuccess(cartCheckedProductVo);
    }

    /**
     * 封装订单Vo
     *
     * @param order
     * @param orderItemList
     * @return
     */
    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();

        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getDesc());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getDesc());
        orderVo.setPostage(order.getPostage());
        orderVo.setShippingId(order.getShippingId());
        // 查询收货地址
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setReceiveName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        // 封装订单明细
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);

        return orderVo;
    }

    /**
     * 封装订单明细Vo
     *
     * @param orderItem
     * @return
     */
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();

        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));

        return orderItemVo;
    }

    /**
     * 封装收货地址Vo
     *
     * @param shipping
     * @return
     */
    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();

        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());

        return shippingVo;
    }

    /**
     * 封装订单明细
     *
     * @param cartList
     * @return
     */
    private ServerResponse assembleOrderItem(List<Cart> cartList) {
        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }

        List<OrderItem> orderItemList = Lists.newArrayList();
        for (Cart cartItem : cartList) {
            OrderItem orderItem = new OrderItem();

            // 查询商品信息
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            // 判断商品状态
            if (Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()) {
                return ServerResponse.createByErrorMessage("商品" + product.getName() + "不是在线售卖状态");
            }
            // 判断商品库存
            if (cartItem.getQuantity() > product.getStock()) {
                return ServerResponse.createByErrorMessage("商品" + product.getName() + "库存不足");
            }

            orderItem.setUserId(cartItem.getUserId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setProductName(product.getName());
            orderItem.setTotalPrice(BigDecimalUtil.mul(orderItem.getQuantity().doubleValue(),
                    orderItem.getCurrentUnitPrice().doubleValue()));
            orderItemList.add(orderItem);
        }

        return ServerResponse.createBySuccess(orderItemList);
    }

    /**
     * 计算购买商品总价
     *
     * @param orderItemList
     * @return
     */
    private BigDecimal calculatePayment(List<OrderItem> orderItemList) {
        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }

        return payment;
    }

    /**
     * 生成订单号
     *
     * @return
     */
    private Long generateOrderNo() {
        Long currentTime = System.currentTimeMillis();
        Random random = new Random();

        return currentTime + random.nextInt(100);
    }

    /**
     * 封装订单
     *
     * @param userId
     * @param shippingId
     * @param payment
     * @return
     */
    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setPayment(payment);
        order.setPostage(0);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());

        int rowCount = orderMapper.insert(order);
        if (rowCount > 0) {
            return order;
        }

        return null;
    }

    /**
     * 减少商品库存数量
     *
     * @param orderItemList
     */
    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    /**
     * 删除购物车勾选的商品
     *
     * @param cartList
     */
    private void clearCheckedCart(List<Cart> cartList) {
        for (Cart cartItem : cartList) {
            cartMapper.deleteByPrimaryKey(cartItem.getId());
        }
    }
}
