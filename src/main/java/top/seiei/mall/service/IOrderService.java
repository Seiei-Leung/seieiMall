package top.seiei.mall.service;

import com.github.pagehelper.PageInfo;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.vo.OrderVo;

import java.math.BigDecimal;
import java.util.Map;

public interface IOrderService {

    ServerResponse<Map<String, String>> pay(Integer userId, Long orderNo, String path);

    ServerResponse alipayCallBack(Map<String, String> params);

    ServerResponse<Boolean> queryOrderStatus(Integer userId, Long orderNo);

    ServerResponse createdOrder(Integer userId, Integer shippingId);

    ServerResponse cancelOrder(Integer userId, Long orderNo);

    ServerResponse<PageInfo> getOrderList(Integer userId, int pageIndex, int pageSize);

    ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo);

    ServerResponse completeOrder(Integer userId, Long orderNo);

    ServerResponse<PageInfo> getAllOrderOfManage(int pageIndex, int pageSize);

    ServerResponse<OrderVo> getByOrderNoOfManage(Long orderno);

    ServerResponse sendGoodsByManage(Long orderNo, Long expressNo, String expressCompany, BigDecimal expresspay);

    ServerResponse applyRefundOrExchangeGoods(Integer userId, Long orderNo, Integer orderItemId, Integer applyType, String reason);

    ServerResponse<PageInfo> getAllRefundOrderByManage(Integer pageIndex, Integer pageSize);

    ServerResponse<PageInfo> getAllExchangeOrderByManage(Integer pageIndex, Integer pageSize);

    ServerResponse queryExpressNoByOrderNo(Integer userId, Long orderNo);

    ServerResponse refundByManage(Long orderNo, String orderItemIdListStr);
}
