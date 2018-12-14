package top.seiei.mall.service.impl;

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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import top.seiei.mall.bean.Order;
import top.seiei.mall.bean.OrderItem;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.dao.OrderItemMapper;
import top.seiei.mall.dao.OrderMapper;
import top.seiei.mall.service.IOrderService;
import top.seiei.mall.util.BigDecimalUtils;
import top.seiei.mall.util.FtpUtil;
import top.seiei.mall.util.PropertiesUtil;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    private static Log logger = LogFactory.getLog(OrderServiceImpl.class);

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderItemMapper orderItemMapper;

    public ServerResponse<Map<String, String>> pay(Integer userId, Long orderNo, String path) {
        // 检验是否有这订单号
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createdByErrorMessage("查无此订单");
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

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("properties/zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        AlipayTradeService tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();

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
                boolean ftpIsSucess = FtpUtil.uploadFile("qrCode", Lists.newArrayList(targetFile));
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