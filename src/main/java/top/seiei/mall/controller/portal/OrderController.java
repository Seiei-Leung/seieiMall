package top.seiei.mall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import top.seiei.mall.bean.User;
import top.seiei.mall.common.Const;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.service.IOrderService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单模块
 */
@Controller
@RequestMapping("/order/")
public class OrderController {

    private Log logger = LogFactory.getLog(OrderController.class);

    @Resource
    private IOrderService iOrderService;

    /**
     * 支付接口，调用支付宝当面付功能，生成二维码图片，并显示
     * @param session session 对象
     * @param orderno 订单号
     * @param request request 主要用于获取 Tomcat 服务器路径，用于存放暂时二维码图片文件
     * @return 返回 Map 类型，包含订单号以及支付二维码图片在 ftp 服务器的路径
     */
    @RequestMapping(value = "pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderno, HttpServletRequest request) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户还没登录");
        }
        // 根据相对路径获取服务器上资源的绝对路径
        String path = request.getSession().getServletContext().getRealPath("upload");
        return iOrderService.pay(user.getId(), orderno, path);
    }

    /**
     * 支付宝订单状态变化回调接口（默认只有支付成功，才会回调通知）
     * @param request request 对象
     * @return 必须返回 'success' 或 'fail'
     */
    @RequestMapping(value = "alipay_callback.do", method = RequestMethod.POST)
    @ResponseBody
    public Object alipayCallBack(HttpServletRequest request) {

        // 获取支付宝回调传入的参数集合
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String item : requestParams.keySet()) {
            String[] values = requestParams.get(item);
            String valueStr = "";
            // 拼接接受参数，查看官方文档，回调过来的参数其实都是单个的，也就是说其实无需循环 String[] 数组，但是以下的拼接方法值得学习
            for (int i = 0; i < values.length; i++) {
                // 使用判断 i == (values.length - 1)，即可以避免末尾逗号的存在
                valueStr = i == (values.length - 1) ? valueStr + values[i] : valueStr + values[i] + i;
            }
            params.put(item, valueStr);
        }
        // 打印日志
        logger.info(String.format("支付宝回调，sign：%s, trade_status：%s, 参数：", params.get("sign"), params.get("trade_status"), params.toString()));

        // 返回结果的验签
        // 除去sign、sign_type两个参数，其中除去 sign 参数，会在调用 AlipaySignature.rsaCheckV2 进行验签的时候去除
        params.remove("sign_type");
        try {
            // Configs 已经在 IOrderServiceImpl 中配置初始化了
            // https://docs.open.alipay.com/194/105322/
            boolean signVerified = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
            // 验签失败
            if (!signVerified) {
                return  ServerResponse.createdBySucessMessage("非法请求！！！");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝验证回调异常",e);
        }
        // 同时官网还说，除了验签，还需要与自身数据库中的订单号，金额等做检验，并且检验是否支付宝重复回调
        ServerResponse serverResponse = iOrderService.alipayCallBack(params);

        if (serverResponse.isSuccess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    /**
     * 前端轮询获取商品的支付状态接口
     * @param session session 对象
     * @param orderNo 订单号
     * @return 是否支付成功，已支付返回 true，未支付返回 false
     */
    @RequestMapping("query_order_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderStatus(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户还没登录");
        }
        return iOrderService.queryOrderStatus(user.getId(), orderNo);
    }


}
