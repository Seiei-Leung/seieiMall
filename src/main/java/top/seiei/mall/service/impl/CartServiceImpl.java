package top.seiei.mall.service.impl;

import org.springframework.stereotype.Service;
import top.seiei.mall.bean.Cart;
import top.seiei.mall.bean.Product;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.dao.CartMapper;
import top.seiei.mall.dao.ProductMapper;
import top.seiei.mall.service.ICartService;
import top.seiei.mall.vo.CartVo;

import javax.annotation.Resource;

@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Resource
    private CartMapper cartMapper;

    @Resource
    private ProductMapper productMapper;

    public ServerResponse addProduct(Integer userId, Integer productId, Integer count) {
        if (userId == null || productId == null || count == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createdByErrorMessage("该商品不存在，添加购物车失败");
        }
        if (count.compareTo(product.getStock()) > 0) {
            return ServerResponse.createdByErrorMessage("货存不足，添加购物车失败");
        }
        Cart cartTemp = cartMapper.selectByUserIdAndProductId(userId, productId);
        Cart cart = new Cart();
        Integer result;
        if (cartTemp == null) {
            cart.setChecked(true);
            cart.setProductId(productId);
            cart.setUserId(userId);
            cart.setQuantity(count);
            result = cartMapper.insertSelective(cart);
        } else {
            cart.setId(cartTemp.getId());
            cart.setQuantity(count);
            result = cartMapper.updateByPrimaryKeySelective(cart);
        }
        if (result != 0) {
            return  ServerResponse.createdBySucessMessage("添加购物车成功");
        } else {
            return ServerResponse.createdByErrorMessage("添加购物车失败");
        }
    }
}
