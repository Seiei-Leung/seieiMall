package top.seiei.mall.service;

import top.seiei.mall.bean.Shipping;
import top.seiei.mall.common.ServerResponse;

public interface IShippingService {

    ServerResponse<Integer> add(Integer userId, Shipping shipping);
}
