package top.seiei.mall.service;

import top.seiei.mall.common.ServerResponse;

public interface ICartService {
    ServerResponse addProduct(Integer userId, Integer productId, Integer count);
}
