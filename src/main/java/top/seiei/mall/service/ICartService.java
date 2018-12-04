package top.seiei.mall.service;

import top.seiei.mall.common.ServerResponse;

import java.util.List;

public interface ICartService {
    ServerResponse addOrUpdateProduct(Integer userId, Integer productId, Integer count, Boolean isChecked);

    ServerResponse deleteProduct(Integer userId, List<Integer> productIdList);

}
