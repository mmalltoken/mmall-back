package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartCheckedProductVo;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 描述：订单业务实现类
 * 作者：NearJC
 * 时间：2019.4.28
 */
@Service("orderService")
@Slf4j
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
    @Autowired
    private PayInfoMapper payInfoMapper;

    // 支付宝当面付2.0服务
    private static AlipayTradeService tradeService;

    static {
        // Configs读取配置信息
        Configs.init("alipayInfo.properties");
        // 使用Configs提供的默认参数,tradeService可以使用单例或者为静态成员对象，不需要反复new
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

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
    public ServerResponse<CartCheckedProductVo> getCartCheckedProduct(Integer userId) {
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
     * 获取订单详情
     *
     * @param orderNo
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<OrderVo> getOrderDetail(Long orderNo, Integer userId) {
        if (orderNo == null) {
            return ServerResponse.createByErrorMessage("获取订单详情参数错误");
        }

        // 查询订单
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }

        // 获取订单明细
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(orderNo, userId);
        if (CollectionUtils.isEmpty(orderItemList)) {
            return ServerResponse.createByErrorMessage("订单明细为空");
        }

        OrderVo orderVo = assembleOrderVo(order, orderItemList);

        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 根据订单号获取订单详情
     *
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse getOrderDetail(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }

        return ServerResponse.createBySuccess(order);
    }

    /**
     * 订单列表
     *
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> orderList(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<Order> orderList = orderMapper.selectByUserId(userId);
        if (CollectionUtils.isEmpty(orderList)) {
            return ServerResponse.createByErrorMessage("查询失败");
        }
        List<OrderVo> orderVoList = assembleOrderVoList(userId, orderList);

        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 订单支付
     *
     * @param orderNo
     * @param userId
     * @param path
     * @return
     */
    @Override
    public ServerResponse orderPay(Long orderNo, Integer userId, String path) {
        if (orderNo == null) {
            return ServerResponse.createByErrorMessage("支付参数错误");
        }

        // 存储响应结果
        Map<String, String> resultMap = Maps.newHashMap();

        // 查询订单
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));

        // 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        String outTradeNo = order.getOrderNo().toString();
        //  订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("happyMmall商城扫码支付，订单号：").append(outTradeNo).toString();
        // 订单总金额，单位为元，不能超过1亿元
        String totalAmount = order.getPayment().toString();
        // 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        String undiscountableAmount = "0";
        // 卖家支付宝账号ID
        String sellerId = "";
        // 订单描述
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").
                append(totalAmount).append("元").toString();
        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";
        // 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";
        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500 1");
        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 查询订单明细
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(orderNo, userId);
        // 商品明细列表
        List<GoodsDetail> goodsDetailList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            // 创建商品信息
            GoodsDetail goodsDetail = GoodsDetail.newInstance(orderItem.getProductId().toString(),                       // 商品id
                    orderItem.getProductName(),                                                                          // 名称
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double("100")).longValue(),// 单价（单位为分）
                    orderItem.getQuantity());                                                                            // 数量
            // 商品添加至商品明细列表
            goodsDetailList.add(goodsDetail);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl("http://yj3kwf.natappfree.cc/order/alipay_callback.do")//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                // 需要修改为运行机器上的路径
                File tempFolder = new File(path);
                if (!tempFolder.exists()) {
                    tempFolder.canWrite();
                    tempFolder.mkdirs();
                }

                // 生成二维码并保存到本地目录
                String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png", response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);
                // 将二维码存储到FTP服务器
                File targetFile = new File(path, qrFileName);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    log.error("上传二维码异常");
                }
                // 设置二维码访问路径
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName();
                // 删除本地二维码
                targetFile.delete();

                log.info("qrPath:" + qrPath);
                resultMap.put("qrPath", qrUrl);

                return ServerResponse.createBySuccess(resultMap);

            case FAILED:
                log.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败");

            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知");

            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常");
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }

    /**
     * 支付宝回调，修改订单状态
     *
     * @param params
     * @return
     */
    @Override
    public ServerResponse aliCallback(Map<String, String> params) {
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeStatus = params.get("trade_status");
        String tradeNo = params.get("trade_no");

        // 查询订单
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("非本网站的订单，回调忽略");
        }
        if (Const.OrderStatusEnum.PAID.getCode() == order.getStatus()) {
            return ServerResponse.createByErrorMessage("支付宝重复调用");
        }

        // 更新为支付状态
        if (Const.AlipayCallback.TRADE_SUCCESS.equals(tradeStatus)) {
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            orderMapper.updateByPrimaryKeySelective(order);
        }

        // 添加支付信息
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformStatus(tradeStatus);
        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }

    /**
     * 查询订单状态
     *
     * @param userId
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }

        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccess();
        }

        return ServerResponse.createByError();
    }

    //-----------------------------后台业务------------------------------------

    /**
     * 后台-订单详情
     *
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse<OrderVo> managerDetail(Long orderNo) {
        if (orderNo == null) {
            return ServerResponse.createByErrorMessage("获取订单详情参数错误");
        }

        // 查询订单
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }

        // 获取订单明细
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        if (CollectionUtils.isEmpty(orderItemList)) {
            return ServerResponse.createByErrorMessage("订单明细为空");
        }

        OrderVo orderVo = assembleOrderVo(order, orderItemList);

        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 后台-查询订单列表
     *
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<Order> orderList = orderMapper.selectByCondition(orderNo);
        if (CollectionUtils.isEmpty(orderList)) {
            return ServerResponse.createByErrorMessage("查询失败");
        }
        List<OrderVo> orderVoList = assembleOrderVoList(null, orderList);

        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);

        return ServerResponse.createBySuccess(pageInfo);
    }

    /**
     * 订单发货
     *
     * @param orderNo
     * @return
     */
    @Override
    public ServerResponse<String> manageDeliverGoods(Long orderNo) {
        if (orderNo == null) {
            return ServerResponse.createByErrorMessage("订单发货参数错误");
        }

        // 查询订单
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("订单不存在");
        }

        if (Const.OrderStatusEnum.PAID.getCode() == order.getStatus()) {
            Order updateOrder = new Order();
            updateOrder.setId(order.getId());
            updateOrder.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
            updateOrder.setSendTime(new Date());

            int rowCount = orderMapper.updateByPrimaryKeySelective(updateOrder);
            if (rowCount > 0) {
                return ServerResponse.createBySuccess("发货成功");
            }
        }

        return ServerResponse.createByErrorMessage("发货失败");
    }

    /**
     * 关闭订单
     *
     * @param hour
     */
    @Override
    public void closeOrder(int hour) {
        Date closeDate = DateUtils.addDays(new Date(), -hour);
        // 查询订单
        List<Order> orderList = orderMapper.selectByStatusAndCreateTime(Const.OrderStatusEnum.NO_PAY.getCode(),
                closeDate);
        if (CollectionUtils.isNotEmpty(orderList)) {
            for (Order order : orderList) {
                // 查询订单明细
                List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(order.getOrderNo(), order.getUserId());

                for (OrderItem orderItem : orderItemList) {
                    // 获取商品库存
                    Integer stock = productMapper.selectStockByPrimaryKey(orderItem.getProductId());
                    // 订单中购买的商品被删除
                    if (stock == null) {
                        continue;
                    }

                    // 更新商品数量
                    Product product = new Product();
                    product.setId(orderItem.getProductId());
                    product.setStock(stock + orderItem.getQuantity());
                    productMapper.updateByPrimaryKeySelective(product);
                }

                // 关闭订单
                orderMapper.closeOrderByPrimaryKey(order.getId());
                log.info("关闭订单的订单号：{}",order.getOrderNo());
            }
        }
    }


    /**
     * 封闭订单Vo列表
     *
     * @param userId
     * @param orderList
     * @return
     */
    private List<OrderVo> assembleOrderVoList(Integer userId, List<Order> orderList) {
        List<OrderVo> orderVoList = Lists.newArrayList();
        // 查询订单明细
        for (Order order : orderList) {
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNoAndUserId(order.getOrderNo(), userId);
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }

        return orderVoList;
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
