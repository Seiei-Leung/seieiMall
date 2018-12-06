package top.seiei.mall.service;

import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.vo.CartVo;

import java.util.List;

public interface ICartService {
    ServerResponse addOrUpdateProduct(Integer userId, Integer productId, Integer count, Boolean isChecked);

    ServerResponse deleteProduct(Integer userId, List<Integer> productIdList);

    ServerResponse<CartVo> getCartList(Integer userId);

    ServerResponse checkOrUnCheck(Integer userId, Integer productId, Boolean isChecked);

    ServerResponse<Integer> getCartCount(Integer userId);
}
