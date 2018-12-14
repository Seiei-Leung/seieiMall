package top.seiei.mall.service;

import top.seiei.mall.common.ServerResponse;

import java.util.Map;

public interface IOrderService {

    ServerResponse<Map<String, String>> pay(Integer userId, Long orderNo, String path);

    ServerResponse alipayCallBack(Map<String, String> params);
}
