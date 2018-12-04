package top.seiei.mall.service.impl;

import org.springframework.stereotype.Service;
import top.seiei.mall.bean.Cart;
import top.seiei.mall.bean.Product;
import top.seiei.mall.common.Const;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.dao.CartMapper;
import top.seiei.mall.dao.ProductMapper;
import top.seiei.mall.service.ICartService;
import top.seiei.mall.vo.CartVo;

import javax.annotation.Resource;
import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Resource
    private CartMapper cartMapper;

    @Resource
    private ProductMapper productMapper;

    /**
     * 用户添加商品到购物车
     * @param userId 用户id
     * @param productId 商品id
     * @param count 商品个数
     * @param isChecked 是否已经勾选
     * @return 响应对象
     */
    public ServerResponse addOrUpdateProduct(Integer userId, Integer productId, Integer count, Boolean isChecked) {
        // 当用户id，商品名id或商品个数少于零时
        if (userId == null || productId == null || !(count.compareTo(0) > 0)) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }
        // 检测商品是否存在，是否在售状态及是否拥有足够的货存
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null || product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createdByErrorMessage("添加购物车失败，该商品不存在或已下架");
        }
        if (count.compareTo(product.getStock()) > 0) {
            return ServerResponse.createdByErrorMessage("添加购物车失败，该商品货存不足");
        }
        // 查看该用户购物车是否已经存在该商品
        Cart cartTemp = cartMapper.selectByUserIdAndProductId(userId, productId);
        Cart cart = new Cart();
        Integer result;
        if (cartTemp == null) {
            cart.setChecked(isChecked);
            cart.setProductId(productId);
            cart.setUserId(userId);
            cart.setQuantity(count);
            result = cartMapper.insertSelective(cart);
        } else {
            cart.setChecked(isChecked);
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

    /**
     * 购物车批量删除商品
     * @param userId 用户 id
     * @param productIdList 商品id 数组
     * @return 响应对象
     */
    public ServerResponse deleteProduct(Integer userId, List<Integer> productIdList) {
        // 当用户id，商品名id 为null
        if (userId == null || productIdList == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }
        int result = cartMapper.deleteByUserIdAndProductId(userId, productIdList);
        if (result > 0) {
            return ServerResponse.createdBySucessMessage("购物车删除商品成功");
        }
        return ServerResponse.createdByErrorMessage("购物车删除商品失败");
    }

}
