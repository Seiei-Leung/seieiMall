package top.seiei.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;
import top.seiei.mall.vo.OrderItemVo;
import top.seiei.mall.vo.ShippingVo;

import java.math.BigDecimal;
import java.util.*;

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
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import top.seiei.mall.bean.*;
import top.seiei.mall.common.Const;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.dao.*;
import top.seiei.mall.service.IOrderService;
import top.seiei.mall.util.BigDecimalUtils;
import top.seiei.mall.util.FtpUtil;
import top.seiei.mall.util.PropertiesUtil;
import top.seiei.mall.vo.OrderVo;

import javax.annotation.Resource;
import java.io.File;

@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    private static Log logger = LogFactory.getLog(OrderServiceImpl.class);

    private static AlipayTradeService tradeService;

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("properties/zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    @Resource
    private ShippingMapper shippingMapper;

    @Resource
    private CartMapper cartMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderItemMapper orderItemMapper;

    @Resource
    private PayInfoMapper payInfoMapper;

    /**
     * 用户创建订单（总订单和子订单）
     * 进阶还可能要传入的参数有：支付平台，邮费
     * @param userId     用户 ID
     * @param shippingId 收货地址 ID
     * @return OrderVo 对象
     */
    @Transactional
    public ServerResponse createdOrder(Integer userId, Integer shippingId) {
        // 检测有没有该地址
        Shipping shipping = shippingMapper.selectByPrimaryKey(shippingId);
        if (shipping == null) {
            return ServerResponse.createdByErrorMessage("无法提交订单，收货地址为空");
        }
        // 获取当前用户购物车列表中已勾选的商品列表
        List<Cart> cartList = cartMapper.selectCheckedByUserId(userId);
        // 已勾选的购物车商品列表转化为 OrderItem 列表
        ServerResponse serverResponseOfOrderItemList = creatOrderItemByCart(cartList, userId);
        if (!serverResponseOfOrderItemList.isSuccess()) {
            return serverResponseOfOrderItemList;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponseOfOrderItemList.getData();
        if (CollectionUtils.isEmpty(orderItemList)) {
            return ServerResponse.createdByErrorMessage("购物车没有勾选购买的商品");
        }
        // 生成订单单号
        Long orderNo = this.generateOrderNo();
        // 计算父订单总价并赋值父订单单号
        BigDecimal payCount = new BigDecimal("0");
        for (OrderItem item : orderItemList) {
            payCount = BigDecimalUtils.add(payCount.doubleValue(), item.getTotalPrice().doubleValue());
            item.setOrderNo(orderNo);
        }
        // 生成父订单并储存到数据库
        ServerResponse<Order> serverResponseOfOrder = this.assembleOrder(orderNo, userId, shippingId, payCount);
        if (!serverResponseOfOrder.isSuccess()) {
            return serverResponseOfOrder;
        }
        Order order = serverResponseOfOrder.getData();
        // 子订单储存到数据库
        int result = orderItemMapper.batchInsert(orderItemList);
        if (!(result < 0)) {
            return ServerResponse.createdByErrorMessage("新增子订单到数据库失败");
        }
        // 减少库存
        this.reduceProductStock(orderItemList);
        // 清空购物车
        this.clearCart(cartList);
        return ServerResponse.createdBySuccess(this.assembleOrderVo(order, orderItemList, shipping));
    }

    /**
     * 在线删除未付款的订单
     * @param userId 用户 ID
     * @param orderNo 订单号
     * @return 是否删除成功
     */
    @Transactional
    public ServerResponse cancelOrder(Integer userId, Long orderNo) {
        // 检验是否有这订单号
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createdByErrorMessage("查无此订单");
        }
        if (order.getStatus() < Const.OrderStatusEnum.ORDER_SUCCESS.getCode()) {
            return ServerResponse.createdByErrorMessage("该订单尚在交易状态，不能删除");
        }
        // 数据库删除该订单及其订单的详情
        orderMapper.deleteByPrimaryKey(order.getId());
        orderItemMapper.deleteByOrderNo(order.getOrderNo());
        return ServerResponse.createdBySucessMessage("删除订单成功");
    }

    /**
     * 获取所有父订单的列表
     * @param userId 用户 ID
     * @param pageIndex 初始页
     * @param pageSize 一页容量
     * @return 所有父订单的列表
     */
    @Transactional
    public ServerResponse<PageInfo> getOrderList(Integer userId, int pageIndex, int pageSize) {
        PageHelper.startPage(pageIndex, pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = assembleOrderVoList(orderList);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createdBySuccess(pageInfo);
    }

    /**
     * 获取订单详情
     * @param userId 用户 ID
     * @param orderNo 订单号
     * @return 订单详情
     */
    @Transactional
    public ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createdByErrorMessage("该订单不存在");
        }
        // 检查订单有效时间
        if (new Date().getTime() > order.getCloseTime().getTime() && order.getStatus() < Const.OrderStatusEnum.PAID.getCode()) {
            order.setStatus(Const.OrderStatusEnum.ORDER_CLOSE.getCode());
            orderMapper.updateByPrimaryKey(order);
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
        Shipping shipping = shippingMapper.selectByUseIdAndShippingId(userId, order.getShippingId());
        OrderVo orderVo = assembleOrderVo(order, orderItemList, shipping);
        return ServerResponse.createdBySuccess(orderVo);
    }

    /**
     * 用户提交确认收货，该订单交易完成
     * @param userId 用户 ID
     * @param orderNo 订单号
     * @return 是否确认成功
     */
    public ServerResponse completeOrder(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createdByErrorMessage("该订单不存在");
        }
        // 判断该订单的状态
        if (order.getStatus() <= Const.OrderStatusEnum.SHIPPED.getCode() || order.getStatus() >= Const.OrderStatusEnum.ORDER_SUCCESS.getCode()) {
            return ServerResponse.createdByErrorMessage("该订单尚不能确认交易完成，或该订单已经关闭");
        }
        order.setStatus(Const.OrderStatusEnum.ORDER_SUCCESS.getCode());
        order.setEndTime(new Date());
        int result = orderMapper.updateByPrimaryKey(order);
        if (result > 0) {
            return ServerResponse.createdBySucessMessage("确认订单交易完成");
        }
        return ServerResponse.createdByErrorMessage("确认订单交易完成失败");
    }

    /**
     * todo 退款
     * @param userId 用户 ID
     * @param orderNo 订单号
     * @param orderItemId 子订单 ID
     * @return
     */
    public ServerResponse applyRefund(Integer userId, Long orderNo, Integer orderItemId) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        OrderItem orderItem = orderItemMapper.selectByPrimaryKey(orderItemId);
        if (order == null || orderItem == null) {
            return ServerResponse.createdByErrorMessage("该订单不存在");
        }
        // 判断该订单的状态
        if (order.getStatus() < Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createdByErrorMessage("该订单尚未支付，何来退款");
        }
        // TODO 检测该订单可以退款、退货的期限


        return null;
    }

    /**
     * 提交订单后，清空购物车
     * @param cartList Cart 列表
     */
    private void clearCart(List<Cart> cartList) {
        for (Cart item : cartList) {
            cartMapper.deleteByPrimaryKey(item.getId());
        }
    }

    /**
     * 提交订单，减少库存
     * @param orderItemList OrderItem 集合
     */
    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem item : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(item.getProductId());
            product.setStock(product.getStock() - item.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    /**
     * 生成父订单，并储存到数据库
     * @param orderNo    订单号
     * @param userId     用户 ID
     * @param shippingId 收货地址 ID
     * @param payCount   订单总价
     * @return Order 对象
     */
    private ServerResponse<Order> assembleOrder(Long orderNo, Integer userId, Integer shippingId, BigDecimal payCount) {
        // 生成订单对象
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUesrId(userId);
        order.setShippingId(shippingId);
        order.setPayment(payCount);
        order.setPaymentType(Const.PayPlatformEnum.ALIPAY.getCode());
        order.setPostage(0);
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        // 限制支付关闭时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 120);
        order.setCloseTime(calendar.getTime());
        int result = orderMapper.insertSelective(order);
        if (!(result > 0)) {
            return ServerResponse.createdByErrorMessage("新增订单到数据库失败");
        }
        return ServerResponse.createdBySuccess(order);
    }

    /**
     * 生成制单号，时间戳 + 0~100 的随机数
     * @return 制单号
     */
    private Long generateOrderNo() {
        Long nowTime = Calendar.getInstance().getTimeInMillis();
        return nowTime + new Random().nextInt(100);
    }

    /**
     * 从购物车中获取已经勾选的商品，制作成 OrderItem 对象集合
     * @param cartList 购物车已经勾选的集合
     * @param userId   用户 ID
     * @return OrderItem 对象集合
     */
    private ServerResponse creatOrderItemByCart(List<Cart> cartList, Integer userId) {
        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.createdByErrorMessage("购物车为空");
        }
        List<OrderItem> orderItemList = new ArrayList<>();
        // 制作 OrderItem 集合
        for (Cart item : cartList) {
            Product product = productMapper.selectByPrimaryKey(item.getProductId());
            // 检测勾选商品的状态，如果不是在售状态返回错误信息
            if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
                return ServerResponse.createdByErrorMessage("商品" + product.getName() + "不是在线售卖状态");
            }
            // 检验勾选商品的库存
            if (item.getQuantity() > product.getStock()) {
                return ServerResponse.createdByErrorMessage("商品" + product.getName() + "的库存只剩" + product.getStock().toString());
            }
            // 组装 OrderItem 对象集合
            OrderItem orderItem = new OrderItem();
            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtils.multiply(product.getPrice().doubleValue(), item.getQuantity().doubleValue()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createdBySuccess(orderItemList);
    }

    /**
     * 转化为 OrderVo 对象的集合
     * @param orderList Order 集合
     * @return
     */
    private List<OrderVo> assembleOrderVoList(List<Order> orderList) {
        List<OrderVo> orderVoList = new ArrayList<>();
        for (Order item : orderList) {
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(item.getOrderNo());
            OrderVo orderVo = assembleOrderVo(item, orderItemList, null);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }

    /**
     * 转化为 OrderVo 对象
     * @param order         Order 对象
     * @param orderItemList OrderItem 集合
     * @param shipping      shipping 对象
     * @return OrderVo 对象
     */
    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList, Shipping shipping) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());
        orderVo.setPaymentTime(order.getPaymentTime());
        orderVo.setSendTime(order.getSendTime());
        orderVo.setEndTime(order.getEndTime());
        orderVo.setCloseTime(order.getCloseTime());
        orderVo.setCreateTime(order.getCreateTime());
        orderVo.setUpdateTime(order.getUpdateTime());
        orderVo.setImgHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderVo.setShippingVo(new ShippingVo());
        orderVo.setOrderItemVoList(Lists.newArrayList());
        List<OrderItemVo> orderItemVoList = new ArrayList<>();
        for (OrderItem item : orderItemList) {
            orderItemVoList.add(this.assembleOrderItemVo(item));
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        orderVo.setShippingVo(this.assembleShippingVo(shipping));
        return orderVo;
    }

    /**
     * 转化为 OrderItemVo 对象
     * @param orderItem OrerItem 对象
     * @return OrderItemVo 对象
     */
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        if (orderItem != null) {
            orderItemVo.setId(orderItem.getId());
            orderItemVo.setOrderNo(orderItem.getOrderNo());
            orderItemVo.setProductId(orderItem.getProductId());
            orderItemVo.setProductName(orderItem.getProductName());
            orderItemVo.setProductImage(orderItem.getProductImage());
            orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
            orderItemVo.setQuantity(orderItem.getQuantity());
            orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        }
        return orderItemVo;
    }

    /**
     * 转化为 ShippingVo 对象
     * @param shipping Shipping 对象
     * @return ShippingVo 对象
     */
    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        if (shipping != null) {
            shippingVo.setReceiverName(shipping.getReceiverName());
            shippingVo.setReceiverPhone(shipping.getReceiverPhone());
            shippingVo.setReceiverProvince(shipping.getReceiverProvince());
            shippingVo.setReceiverCity(shipping.getReceiverCity());
            shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
            shippingVo.setReceiverAddress(shipping.getReceiverAddress());
            shippingVo.setReceiverZip(shipping.getReceiverZip());
        }
        return shippingVo;
    }

    /**
     * 支付接口，调用支付宝当面付功能，生成二维码图片，并显示
     * @param userId  用户 ID
     * @param orderNo 订单号
     * @param path    二维码图片的 Tomcat 临时存放路径
     * @return 返回 Map 类型，包含订单号以及支付二维码图片在 ftp 服务器的路径
     */
    public ServerResponse<Map<String, String>> pay(Integer userId, Long orderNo, String path) {
        // 检验是否有这订单号
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createdByErrorMessage("查无此订单");
        }
        // 检查订单有效时间
        if (new Date().getTime() > order.getCloseTime().getTime()) {
            order.setStatus(Const.OrderStatusEnum.ORDER_CLOSE.getCode());
            orderMapper.updateByPrimaryKey(order);
            return ServerResponse.createdByErrorMessage("该订单已过期");
        }
        // 返回对象 Map，包括订单号以及扫码支付的二维码URL地址
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("orderNo", order.getOrderNo().toString());

        /**
         * 接入支付宝
         */
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("SeieiMall扫码支付，订单号：").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("共花费：").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        // 获取该订单下的所有商品详细
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        for (OrderItem item : orderItemList) {
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail good = GoodsDetail.newInstance(item.getProductId().toString(), item.getProductName(),
                    BigDecimalUtils.multiply(item.getCurrentUnitPrice().doubleValue(), new Double(100).doubleValue()).longValue(),
                    item.getQuantity());
            goodsDetailList.add(good);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))// 支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            // 返回成功，解析保存二维码到服务器
            case SUCCESS:
                logger.info("支付宝预下单成功: )");
                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                // 查找是否有该目录，没有就创建
                File file = new File(path);
                if (!file.exists()) {
                    file.setWritable(true);
                    file.mkdirs();
                }

                // 需要修改为运行机器上的路径
                String filePath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                String fileName = String.format("qr-%s.png", response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath); // 保存二维码图片
                // 获取当前保存二维码图片的 file 对象，存放到 ftp 服务器中
                File targetFile = new File(path, fileName);
                boolean ftpIsSucess = FtpUtil.uploadFile("img", Lists.newArrayList(targetFile));
                if (!ftpIsSucess) {
                    logger.error("上传二维码图片到ftp服务器失败");
                    return ServerResponse.createdByErrorMessage("上传二维码图片到ftp服务器失败");
                }
                // 删除 tomcat 下的图片
                targetFile.delete();
                logger.info("qrCodeFilePath:" + filePath);
                // 获取二维码 URL 填充到返回结果中
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + "qeCode/" + fileName;
                resultMap.put("qrUrl", qrUrl);
                return ServerResponse.createdBySuccess(resultMap);

            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createdByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createdByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createdByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    /**
     * 用于与自身数据库中的订单号，金额等做检验，并且检验是否支付宝重复回调，同时更新数据库中订单的状态，以及新增支付信息数据表
     * @param params 支付宝回调参数组
     * @return 检验是否正确
     */
    public ServerResponse alipayCallBack(Map<String, String> params) {
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String platformNumber = params.get("trade_no");
        String platformStatus = params.get("trade_status");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createdByErrorMessage("没有该订单");
        }
        // 检查该订单的状态，检测是否为支付宝重复调用
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createdBySucessMessage("支付宝重复调用");
        }
        // 检查支付宝回调的交易状态，如果交易成功，更新订单状态以及支付时间
        if (Const.AlipayCallback.TRADE_SUCCESS.equals(platformStatus)) {
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            order.setPaymentTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }
        // 新增支付信息数据表
        PayInfo payInfo = new PayInfo();
        payInfo.setOrderNo(orderNo);
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(platformNumber);
        payInfo.setUserId(order.getUesrId());
        payInfo.setPlatformStatus(platformStatus);
        payInfoMapper.insert(payInfo);

        return ServerResponse.createdBySuccess();
    }

    /**
     * 前端轮询获取商品的支付状态接口
     *
     * @param userId  用户 ID
     * @param orderNo 订单号
     * @return 是否支付成功，已支付返回 true，未支付返回 false
     */
    public ServerResponse<Boolean> queryOrderStatus(Integer userId, Long orderNo) {
        // 检验是否有这订单号
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createdByErrorMessage("查无此订单");
        }
        Integer status = order.getStatus();
        if (status >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createdBySuccess(true);
        }
        return ServerResponse.createdBySuccess(false);
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    /**
     *
     * 后台接口
     *
      */

    /**
     * 后台接口，获取所有订单（分页）
     * @param pageIndex 页面初始页
     * @param pageSize 一页的容量
     * @return 订单列表
     */
    public ServerResponse<PageInfo> getAllOrderOfManage(int pageIndex, int pageSize) {
        PageHelper.startPage(pageIndex, pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        PageInfo pageInfo = new PageInfo(orderList);
        List<OrderVo> orderVoList = assembleOrderVoList(orderList);
        pageInfo.setList(orderList);
        return ServerResponse.createdBySuccess(pageInfo);
    }

    /**
     * 后台接口，根据订单号获取订单详情
     * @param orderNo 订单号
     * @return OrderVo 对象
     */
    public ServerResponse<OrderVo> getByOrderNoOfManage(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createdByErrorMessage("该订单不存在");
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        OrderVo orderVo = assembleOrderVo(order, orderItemList, shipping);
        return ServerResponse.createdBySuccess(orderVo);
    }

    /**
     * 后端接口，商品发货
     * @param orderNo 订单号
     * @param expressNo 订单号
     * @param expressCompany 快递公司
     * @return
     */
    public ServerResponse sendGoods(Long orderNo, Long expressNo, Integer expressCompany) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createdByErrorMessage("该订单不存在");
        }
        // 正常状态下的发货
        if (order.getStatus() == Const.OrderStatusEnum.PAID.getCode()) {
            order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
            order.setSendTime(new Date());
            // todo 创建快递信息表，内含快递单号，快递公司、订单号，建表时间
            return ServerResponse.createdBySucessMessage("发货成功");
        }
        // 换货状态下的发货
        else if (order.getStatus() == Const.OrderStatusEnum.APPLY_EXCHANGE_GOOD.getCode()) {
            // todo 创建快递信息表，内含快递单号，快递公司、订单号，建表时间
            return ServerResponse.createdBySucessMessage("发货成功");
        }
        return ServerResponse.createdByErrorMessage("发货失败，请检测该订单的状态");
    }
}
