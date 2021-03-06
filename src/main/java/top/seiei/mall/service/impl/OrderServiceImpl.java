package top.seiei.mall.service.impl;

import java.util.Date;

import com.alipay.demo.trade.model.builder.AlipayTradeRefundRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FRefundResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.transaction.annotation.Transactional;
import top.seiei.mall.vo.ExpressVo;
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

    @Resource
    private ExpressMapper expressMapper;

    @Resource
    private EvaluationMapper evaluationMapper;

    /**
     * 用户创建订单（总订单和子订单）
     * 进阶还可能要传入的参数有：支付平台，邮费
     *
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
        // 计算父订单总价、设置子订单单号，设置子订单状态
        BigDecimal payCount = new BigDecimal("0");
        for (OrderItem item : orderItemList) {
            payCount = BigDecimalUtils.add(payCount.doubleValue(), item.getTotalPrice().doubleValue());
            item.setOrderNo(orderNo);
            item.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        }
        // 生成总订单并储存到数据库
        ServerResponse<Order> serverResponseOfOrder = this.assembleOrder(orderNo, userId, shippingId, payCount);
        if (!serverResponseOfOrder.isSuccess()) {
            return serverResponseOfOrder;
        }
        Order order = serverResponseOfOrder.getData();
        // 子订单储存到数据库
        int result = orderItemMapper.batchInsert(orderItemList);
        if (result < 0) {
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
     *
     * @param userId  用户 ID
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
        if (Const.OrderStatusEnum.NO_PAY.getCode() < order.getStatus() && order.getStatus() < Const.OrderStatusEnum.ORDER_SUCCESS.getCode()) {
            return ServerResponse.createdByErrorMessage("该订单尚在交易状态，不能删除");
        }
        // 数据库删除该订单及其订单的详情
        orderMapper.deleteByPrimaryKey(order.getId());
        orderItemMapper.deleteByOrderNo(order.getOrderNo());
        return ServerResponse.createdBySucessMessage("删除订单成功");
    }

    /**
     * 获取所有父订单的列表
     *
     * @param userId    用户 ID
     * @param pageIndex 初始页
     * @param pageSize  一页容量
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
     *
     * @param userId  用户 ID
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
        // 订单支付期限过期
        if (new Date().getTime() > order.getCloseTime().getTime() && order.getStatus() < Const.OrderStatusEnum.PAID.getCode()) {
            order.setStatus(Const.OrderStatusEnum.ORDER_CLOSE.getCode());
            orderMapper.updateByPrimaryKey(order);
            orderItemMapper.batchUpdateStatusByOrderNo(order.getOrderNo(), Const.OrderStatusEnum.ORDER_CLOSE.getCode());
        }
        // 订单交易期限已到，自动确认交易成功
        if (
                order.getEndTime() != null
                        &&
                        new Date().getTime() > order.getEndTime().getTime()
                        &&
                        (order.getStatus() == Const.OrderStatusEnum.SHIPPED.getCode() || order.getStatus() == Const.OrderStatusEnum.RECEIVED.getCode())
        ) {
            order.setStatus(Const.OrderStatusEnum.ORDER_SUCCESS.getCode());
            orderMapper.updateByPrimaryKey(order);
            orderItemMapper.batchUpdateStatusByOrderNo(order.getOrderNo(), Const.OrderStatusEnum.ORDER_SUCCESS.getCode());
        }
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());
        Shipping shipping = shippingMapper.selectByUseIdAndShippingId(userId, order.getShippingId());
        OrderVo orderVo = assembleOrderVo(order, orderItemList, shipping);
        return ServerResponse.createdBySuccess(orderVo);
    }

    /**
     * 用户提交确认收货，该订单交易完成，使用沙箱测试
     *
     * @param userId  用户 ID
     * @param orderNo 订单号
     * @return 是否确认成功
     */
    public ServerResponse completeOrder(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createdByErrorMessage("该订单不存在");
        }
        // 判断该订单的状态
        if (order.getStatus() <= Const.OrderStatusEnum.PAID.getCode() || order.getStatus() >= Const.OrderStatusEnum.ORDER_SUCCESS.getCode()) {
            return ServerResponse.createdByErrorMessage("该订单尚不能确认交易完成，或该订单已经交易完成或关闭");
        }
        Integer status = generateEndStatus(order.getStatus()).getData();
        order.setStatus(status);
        order.setCompleteTime(new Date());
        this.batchUpdateOrderItemStatusByOrderNo(order.getOrderNo());
        int result = orderMapper.updateByPrimaryKey(order);
        if (result > 0) {
            return ServerResponse.createdBySucessMessage("确认订单交易完成");
        }
        return ServerResponse.createdByErrorMessage("确认订单交易完成失败");
    }

    /**
     * 退款或换货申请
     *
     * @param userId      用户 ID
     * @param orderNo     订单号
     * @param orderItemId 子订单 ID
     * @return
     */
    public ServerResponse applyRefundOrExchangeGoods(Integer userId, Long orderNo, Integer orderItemId, Integer applyType, String reason) {
        System.out.println(applyType != Const.OrderStatusEnum.APPLY_EXCHANGE_GOOD.getCode());
        if (applyType != Const.OrderStatusEnum.APPLY_EXCHANGE_GOOD.getCode() && applyType != Const.OrderStatusEnum.APPLY_REFUND.getCode()) {
            return ServerResponse.createdByErrorMessage("申请类型码错误");
        }
        if (StringUtils.isBlank(reason)) {
            return ServerResponse.createdByErrorMessage("原因不能为空");
        }
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        OrderItem orderItem = orderItemMapper.selectByPrimaryKey(orderItemId);
        if (order == null || orderItem == null) {
            return ServerResponse.createdByErrorMessage("该订单不存在");
        }
        // 判断该订单的状态
        if (orderItem.getStatus() < Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createdByErrorMessage("该订单尚未支付");
        }
        if (orderItem.getStatus() > Const.OrderStatusEnum.EXCHANGED_GOOD.getCode()) {
            return ServerResponse.createdByErrorMessage("该订单商家已经停止了交易，不能申请退货及换货，详情请与商家联系");
        }
        // 检测该订单可以退款、退货的期限
        if (order.getEndTime() != null && new Date().getTime() > order.getEndTime().getTime()) {
            return ServerResponse.createdByErrorMessage("该订单已过可以申请退货、退款的期限");
        }
        // 保存退款、退货原因
        Evaluation evaluation = evaluationMapper.selectByOrderItemId(orderItemId);
        if (evaluation == null) {
            evaluation = new Evaluation();
            evaluation.setOrderNo(orderNo);
            evaluation.setOrderItemId(orderItemId);
            evaluation.setProductId(orderItem.getProductId());
            evaluation.setUserId(userId);
            if (applyType == Const.OrderStatusEnum.APPLY_REFUND.getCode()) {
                evaluation.setRefundReason(reason);
            }
            if (applyType == Const.OrderStatusEnum.APPLY_EXCHANGE_GOOD.getCode()) {
                evaluation.setExchangeReason(reason);
            }
            evaluationMapper.insertSelective(evaluation);
        } else {
            if (applyType == Const.OrderStatusEnum.APPLY_REFUND.getCode()) {
                evaluation.setRefundReason(reason);
            }
            if (applyType == Const.OrderStatusEnum.APPLY_EXCHANGE_GOOD.getCode()) {
                evaluation.setExchangeReason(reason);
            }
            evaluationMapper.updateByPrimaryKeySelective(evaluation);
        }
        order.setStatus(applyType);
        orderItem.setStatus(applyType);
        orderMapper.updateByPrimaryKeySelective(order);
        orderItemMapper.updateByPrimaryKeySelective(orderItem);
        return ServerResponse.createdBySucessMessage("成功发送申请");
    }

    /**
     * 根据订单号返回快递信息 ExpressVo 集合
     *
     * @param userId  用户 ID
     * @param orderNo 订单号
     * @return ExpressVo 集合
     */
    public ServerResponse queryExpressNoByOrderNo(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createdByErrorMessage("该订单不存在");
        }
        List<Express> expressList = expressMapper.selectByOrderNo(order.getOrderNo());
        if (CollectionUtils.isEmpty(expressList)) {
            return ServerResponse.createdByErrorMessage("暂时没有该订单的快递信息");
        }
        return ServerResponse.createdBySuccess(assembleExpressVoList(expressList));
    }

    /**
     * 支付接口，调用支付宝当面付功能，生成二维码图片，并显示
     *
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
        // 检查该订单的状态
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createdByErrorMessage("该订单已支付过");
        }
        // 检查该订单有效时间
        if (new Date().getTime() > order.getCloseTime().getTime()) {
            order.setStatus(Const.OrderStatusEnum.ORDER_CLOSE.getCode());
            orderMapper.updateByPrimaryKey(order);
            orderItemMapper.batchUpdateStatusByOrderNo(order.getOrderNo(), Const.OrderStatusEnum.ORDER_CLOSE.getCode());
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
     *
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
            orderItemMapper.batchUpdateStatusByOrderNo(order.getOrderNo(), Const.OrderStatusEnum.PAID.getCode());
        }
        // 新增支付信息数据表
        PayInfo payInfo = new PayInfo();
        payInfo.setOrderNo(orderNo);
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(platformNumber);
        payInfo.setUserId(order.getUserId());
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

    /**
     *
     * 后台接口
     *
     */

    /**
     * 后台接口，获取所有订单（分页）
     *
     * @param pageIndex 页面初始页
     * @param pageSize  一页的容量
     * @return 订单列表
     */
    public ServerResponse<PageInfo> getAllOrderOfManage(int pageIndex, int pageSize) {
        PageHelper.startPage(pageIndex, pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        PageInfo pageInfo = new PageInfo(orderList);
        List<OrderVo> orderVoList = assembleOrderVoList(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createdBySuccess(pageInfo);
    }

    /**
     * 后台接口，根据订单号获取订单详情
     *
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
     *
     * @param orderNo        订单号
     * @param expressNo      订单号
     * @param expressCompany 快递公司
     * @return
     */
    public ServerResponse sendGoodsByManage(Long orderNo, Long expressNo, String expressCompany, BigDecimal expresspay) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createdByErrorMessage("该订单不存在");
        }
        // 正常状态下的发货
        if (order.getStatus() == Const.OrderStatusEnum.PAID.getCode()) {
            order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
            order.setSendTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            orderItemMapper.batchUpdateStatusByOrderNo(order.getOrderNo(), Const.OrderStatusEnum.SHIPPED.getCode());
            Express express = new Express();
            express.setOrderNo(order.getOrderNo());
            express.setExpressCompany(expressCompany);
            express.setExpressPay(expresspay);
            expressMapper.updateByPrimaryKey(express);
            return ServerResponse.createdBySucessMessage("发货成功");
        }
        // 换货状态下的发货
        else if (order.getStatus() == Const.OrderStatusEnum.APPLY_EXCHANGE_GOOD.getCode()) {
            Express express = new Express();
            express.setOrderNo(order.getOrderNo());
            express.setExpressCompany(expressCompany);
            express.setExpressPay(expresspay);
            expressMapper.updateByPrimaryKey(express);
            return ServerResponse.createdBySucessMessage("发货成功");
        }
        return ServerResponse.createdByErrorMessage("发货失败，请检测该订单的状态");
    }

    /**
     * 后台接口，获取所有退款申请的订单
     *
     * @param pageIndex 初始页
     * @param pageSize  一页容量
     * @return OrderVo 集合
     */
    public ServerResponse<PageInfo> getAllRefundOrderByManage(Integer pageIndex, Integer pageSize) {
        PageHelper.startPage(pageIndex, pageSize);
        List<Order> orderList = orderMapper.selectSpecialOrder(Const.OrderStatusEnum.APPLY_REFUND.getCode());
        List<OrderVo> orderVoList = assembleOrderVoList(orderList);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createdBySuccess(pageInfo);
    }

    /**
     * 后台接口，获取所有换货申请的订单
     *
     * @param pageIndex 初始页
     * @param pageSize  一页容量
     * @return OrderVo 集合
     */
    public ServerResponse<PageInfo> getAllExchangeOrderByManage(Integer pageIndex, Integer pageSize) {
        PageHelper.startPage(pageIndex, pageSize);
        List<Order> orderList = orderMapper.selectSpecialOrder(Const.OrderStatusEnum.APPLY_EXCHANGE_GOOD.getCode());
        List<OrderVo> orderVoList = assembleOrderVoList(orderList);
        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);
        return ServerResponse.createdBySuccess(pageInfo);
    }

    /**
     * 后台接口，强制确认交易完成、关闭
     *
     * @param ordernolist 订单号
     * @return 是否成功
     */
    public ServerResponse completeOrderByManage(List<Long> ordernolist) {
        for (Long orderNo : ordernolist) {
            // 检查订单及其状态
            Order order = orderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                return ServerResponse.createdByErrorMessage("没有该订单");
            }
            ServerResponse response = this.generateEndStatus(order.getStatus());
            if (!response.isSuccess()) {
                return response;
            }
            order.setStatus((Integer) response.getData());
            order.setCompleteTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            batchUpdateOrderItemStatusByOrderNo(order.getOrderNo());
        }
        return ServerResponse.createdBySucessMessage("设置成功");
    }

    /**
     * 后台接口，允许换货
     *
     * @param orderNo            订单号
     * @param orderItemIdListStr 子订单 ID 集合，以逗号间隔的字符串形式表示
     * @return 是否设置成功
     */
    public ServerResponse exchangeGoodByManage(Long orderNo, String orderItemIdListStr) {
        // 检查订单状态
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || order.getStatus() != Const.OrderStatusEnum.APPLY_EXCHANGE_GOOD.getCode()) {
            return ServerResponse.createdByErrorMessage("没有该订单");
        }
        if (order.getEndTime().getTime() < new Date().getTime()) {
            order.setStatus(Const.OrderStatusEnum.ORDER_SUCCESS.getCode());
            order.setCompleteTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            orderItemMapper.batchUpdateStatusByOrderNo(order.getOrderNo(), Const.OrderStatusEnum.ORDER_SUCCESS.getCode());
            return ServerResponse.createdByErrorMessage("该订单的可换货时间已过");
        }
        // 传入的 OrderItem ID 集合是以逗号间隔的字符串形式表示
        String[] orderItemIdStrList = orderItemIdListStr.split(",");
        List<OrderItem> orderItemList = new ArrayList<>();
        for (String item : orderItemIdStrList) {
            OrderItem orderItem = orderItemMapper.selectByOrderNoAndId(order.getOrderNo(), Integer.parseInt(item));
            if (orderItem != null && orderItem.getStatus() == Const.OrderStatusEnum.APPLY_EXCHANGE_GOOD.getCode()) {
                orderItem.setStatus(Const.OrderStatusEnum.EXCHANGING_GOOD.getCode());
                orderItemMapper.updateByPrimaryKeySelective(orderItem);
                orderItemList.add(orderItem);
            }
        }
        if (orderItemList.size() == 0) {
            return ServerResponse.createdByErrorMessage("没有找到对应的详情订单");
        }
        order.setStatus(Const.OrderStatusEnum.EXCHANGING_GOOD.getCode());
        orderMapper.updateByPrimaryKeySelective(order);
        return ServerResponse.createdByErrorMessage("设置允许换货成功");
    }

    /**
     * 后台接口，确认退款
     *
     * @param orderNo            订单号
     * @param orderItemIdListStr 子订单 ID 集合，以逗号间隔的字符串形式表示
     * @return 是否退款成功
     */
    public ServerResponse refundByManage(Long orderNo, String orderItemIdListStr) {
        // 检查订单状态
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || order.getStatus() != Const.OrderStatusEnum.APPLY_REFUND.getCode()) {
            return ServerResponse.createdByErrorMessage("没有该订单");
        }
        if (order.getEndTime() != null && order.getEndTime().getTime() < new Date().getTime()) {
            order.setStatus(Const.OrderStatusEnum.ORDER_SUCCESS.getCode());
            order.setCompleteTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
            orderItemMapper.batchUpdateStatusByOrderNo(order.getOrderNo(), Const.OrderStatusEnum.ORDER_SUCCESS.getCode());
            return ServerResponse.createdByErrorMessage("该订单的可退款时间已过");
        }
        // 传入的 OrderItem ID 集合是以逗号间隔的字符串形式表示
        String[] orderItemIdStrList = orderItemIdListStr.split(",");
        List<OrderItem> orderItemList = new ArrayList<>();
        // 计算退款金额
        BigDecimal refundCount = new BigDecimal("0");
        for (String item : orderItemIdStrList) {
            OrderItem orderItem = orderItemMapper.selectByOrderNoAndId(order.getOrderNo(), Integer.parseInt(item));
            if (orderItem != null && orderItem.getStatus() == Const.OrderStatusEnum.APPLY_REFUND.getCode()) {
                refundCount = BigDecimalUtils.add(refundCount.doubleValue(), orderItem.getTotalPrice().doubleValue());
                orderItemList.add(orderItem);
            }
        }
        if (orderItemList.size() == 0) {
            return ServerResponse.createdByErrorMessage("没有找到对应的详情订单");
        }
        // 获取退款理由用以传入给支付宝，默认获取第一个退款子订单的退款理由
        String reason = evaluationMapper.selectByOrderItemId(orderItemList.get(0).getId()).getRefundReason();

        /**
         * 接入支付宝
         */

        // (必填) 外部订单号，需要退款交易的商户外部订单号
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 退款金额，该金额必须小于等于订单的支付金额，单位为元
        String refundAmount = refundCount.toString();

        // (可选，需要支持重复退货时必填) 商户退款请求号，相同支付宝交易号下的不同退款请求号对应同一笔交易的不同退款申请，
        // 对于相同支付宝交易号下多笔相同商户退款请求号的退款交易，支付宝只会进行一次退款
        String outRequestNo = "";

        // (必填) 退款原因，可以说明用户退款原因，方便为商家后台提供统计
        // 获取
        String refundReason = reason;

        // (必填) 商户门店编号，退款情况下可以为商家后台提供退款权限判定和统计等作用，详询支付宝技术支持
        String storeId = "test_store_id";

        // 创建退款请求builder，设置请求参数
        AlipayTradeRefundRequestBuilder builder = new AlipayTradeRefundRequestBuilder()
                .setOutTradeNo(outTradeNo).setRefundAmount(refundAmount).setRefundReason(refundReason)
                .setOutRequestNo(outRequestNo).setStoreId(storeId);

        AlipayF2FRefundResult result = tradeService.tradeRefund(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info(String.format("支付宝退款成功，退款订单号为%s，退款金额为%s元", order.getOrderNo().toString(), refundCount.toString()));
                order.setStatus(Const.OrderStatusEnum.REFUNDED.getCode());
                order.setCompleteTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
                for (OrderItem item : orderItemList) {
                    item.setStatus(Const.OrderStatusEnum.REFUNDED.getCode());
                    orderItemMapper.updateByPrimaryKeySelective(item);
                }
                return ServerResponse.createdBySucessMessage(String.format("支付宝退款成功，退款订单号为%s，退款金额为%s元", order.getOrderNo().toString(), refundCount.toString()));
            case FAILED:
                logger.error("支付宝退款失败!!!");
                return ServerResponse.createdByErrorMessage("支付宝退款失败!!!");
            case UNKNOWN:
                logger.error("系统异常，订单退款状态未知!!!");
                return ServerResponse.createdByErrorMessage("系统异常，订单退款状态未知!!!");
            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createdByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    /**
     *  私有方法
     *
     */

    /**
     * 提交订单后，清空购物车
     *
     * @param cartList Cart 列表
     */
    private void clearCart(List<Cart> cartList) {
        for (Cart item : cartList) {
            cartMapper.deleteByPrimaryKey(item.getId());
        }
    }

    /**
     * 提交订单，减少库存
     *
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
     *
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
        order.setUserId(userId);
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
     * 强制将某个订单中子订单的交易状态设置为成功或关闭
     *
     * @param orderno 订单号
     */
    private void batchUpdateOrderItemStatusByOrderNo(Long orderno) {
        List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderno);
        for (OrderItem item : orderItemList) {
            ServerResponse<Integer> response = generateEndStatus(item.getStatus());
            if (response.isSuccess()) {
                Integer status = response.getData();
                item.setStatus(status);
                orderItemMapper.updateByPrimaryKeySelective(item);
            }
        }
    }

    /**
     * 生成制单号，时间戳 + 0~100 的随机数
     *
     * @return 制单号
     */
    private Long generateOrderNo() {
        Long nowTime = Calendar.getInstance().getTimeInMillis();
        return nowTime + new Random().nextInt(100);
    }

    /**
     * 从购物车中获取已经勾选的商品，制作成 OrderItem 对象集合
     *
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
     *
     * @param orderList Order 集合
     * @return OrderVo 对象的集合
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
     *
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
        orderVo.setCompleteTime(order.getCompleteTime());
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
     *
     * @param orderItem OrerItem 对象
     * @return OrderItemVo 对象
     */
    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        if (orderItem != null) {
            orderItemVo.setOrderItemId(orderItem.getId());
            orderItemVo.setOrderNo(orderItem.getOrderNo());
            orderItemVo.setStatus(Const.OrderStatusEnum.codeOf(orderItem.getStatus()).getValue());
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
     *
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
     * Express 转化为 ExpressVo
     *
     * @param express Express 对象
     * @return ExpressVo 对象
     */
    private ExpressVo assembleExpressVo(Express express) {
        ExpressVo expressVo = new ExpressVo();
        expressVo.setOrderNo(express.getOrderNo());
        expressVo.setExpressNo(express.getExpressNo());
        expressVo.setExpressCompany(express.getExpressCompany());
        expressVo.setExpressPay(express.getExpressPay());
        expressVo.setCreateTime(express.getCreateTime());
        return expressVo;
    }

    /**
     * Express 集合转化为 ExpressVo 集合
     *
     * @param expressList Express 集合
     * @return ExpressVo 集合
     */
    private List<ExpressVo> assembleExpressVoList(List<Express> expressList) {
        List<ExpressVo> expressVoList = new ArrayList<>();
        for (Express item : expressList) {
            expressVoList.add(assembleExpressVo(item));
        }
        return expressVoList;
    }

    /**
     * 不同订单状态码最后转化成不同的交易状态码
     *
     * @param status 该订单现在的状态码
     * @return 交易状态码
     */
    private ServerResponse<Integer> generateEndStatus(Integer status) {
        if (status <= Const.OrderStatusEnum.PAID.getCode()) {
            return ServerResponse.createdByErrorMessage("该订单尚不能设置成功或关闭状态");
        }
        if (status >= Const.OrderStatusEnum.ORDER_SUCCESS.getCode()) {
            return ServerResponse.createdByErrorMessage("该订单已经为成功或关闭状态");
        }
        // 正常状态下，将订单状态设置为交易成功（已发货或已收货状态）
        if (status < Const.OrderStatusEnum.APPLY_REFUND.getCode()) {
            return ServerResponse.createdBySuccess(Const.OrderStatusEnum.ORDER_SUCCESS.getCode());
        }
        // 不允许退款状态下，将订单状态设置为交易成功
        if (status == Const.OrderStatusEnum.APPLY_REFUND.getCode()) {
            return ServerResponse.createdBySuccess(Const.OrderStatusEnum.NO_REFUND.getCode());
        }
        // 不允许换货状态下，将订单状态设置为交易成功
        if (status == Const.OrderStatusEnum.APPLY_EXCHANGE_GOOD.getCode()) {
            return ServerResponse.createdBySuccess(Const.OrderStatusEnum.NO_EXCHANGE_GOOD.getCode());
        }
        // 允许换货状态下，将订单状态设置为交易成功
        if (status == Const.OrderStatusEnum.EXCHANGING_GOOD.getCode()) {
            return ServerResponse.createdBySuccess(Const.OrderStatusEnum.EXCHANGED_GOOD.getCode());
        }
        return ServerResponse.createdByErrorMessage("传入状态码错误！");
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
}
