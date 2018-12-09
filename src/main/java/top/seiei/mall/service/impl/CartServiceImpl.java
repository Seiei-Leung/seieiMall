package top.seiei.mall.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import top.seiei.mall.bean.Cart;
import top.seiei.mall.bean.Product;
import top.seiei.mall.common.Const;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.dao.CartMapper;
import top.seiei.mall.dao.ProductMapper;
import top.seiei.mall.service.ICartService;
import top.seiei.mall.util.BigDecimalUtils;
import top.seiei.mall.util.PropertiesUtil;
import top.seiei.mall.vo.CartProductVo;
import top.seiei.mall.vo.CartVo;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
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
            if (result != 0) {
                return  ServerResponse.createdBySucessMessage("添加购物车成功");
            }
        } else {
            cart.setChecked(isChecked);
            cart.setId(cartTemp.getId());
            cart.setQuantity(count);
            result = cartMapper.updateByPrimaryKeySelective(cart);
            if (result != 0) {
                return  ServerResponse.createdBySucessMessage("修改购买商品信息成功");
            }
        }
        return ServerResponse.createdByErrorMessage("添加购物车失败");
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

    /**
     * 获取用户购物车所有信息（公开方法）
     * @param userId 用户 ID
     * @return 响应对象
     */
    public ServerResponse<CartVo> getCartList(Integer userId) {
        if (userId == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }
        CartVo cartVo = createdCartProductVo(userId);
        return ServerResponse.createdBySuccess(cartVo);
    }

    /**
     * 根据用户 ID 获取其购物车所有信息，返回的 CartVo 对象包括商品列表商品的详情，商品列表已勾选的在线商品的总价，图片的 url host
     * @param userId 用户 ID
     * @return CartVo 对象
     */
    private CartVo createdCartProductVo(Integer userId) {
        CartVo cartVo = new CartVo();
        cartVo.setUserId(userId);
        cartVo.setImgHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        cartVo.setCartTotalPrice(new BigDecimal("0"));
        List<CartProductVo> cartProductVoList = new ArrayList<>();
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        if (CollectionUtils.isNotEmpty(cartList)) {
            for (Cart item : cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setCartId(item.getId());
                cartProductVo.setChecked(item.getChecked());
                cartProductVo.setProductId(item.getProductId());
                Product product = productMapper.selectByPrimaryKey(item.getProductId());
                if (product != null) {
                    // 放进购物车之后，商品的库存也是会变化的
                    // 当购物车商品的数量小于或等于库存的时候
                    if (!(item.getQuantity().compareTo(product.getStock()) > 0)) {
                        cartProductVo.setQuantity(item.getQuantity());
                    } else {
                        cartProductVo.setQuantity(product.getStock());
                        cartProductVo.setHadLimitCount(true);
                    }
                    cartProductVo.setMainImage(product.getMainImage());
                    cartProductVo.setName(product.getName());
                    cartProductVo.setStatus(product.getStatus());
                    cartProductVo.setPrice(product.getPrice());
                    cartProductVo.setStock(product.getStock());
                    cartProductVo.setSubtitle(product.getSubtitle());
                    // 计算总价还包括检测商品是否在线，是否勾选
                    if (product.getStatus() == Const.ProductStatusEnum.ON_SALE.getCode() && item.getChecked()) {
                        cartVo.setCartTotalPrice(cartVo.getCartTotalPrice().add(BigDecimalUtils.multiply(cartProductVo.getPrice().doubleValue(), cartProductVo.getQuantity())));
                    }
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartProductVoList(cartProductVoList);
        return cartVo;
    }

    /**
     * 用于批量或单独勾选或反勾选商品操作
     * @param userId 用户 ID
     * @param productId 商品 ID
     * @param isChecked 是否勾选
     * @return 操作是否成功
     */
    public ServerResponse checkOrUnCheck(Integer userId, Integer productId, Boolean isChecked) {
        if (userId == null || isChecked == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
        }
        int result = cartMapper.checkOrUnCheck(userId, productId, isChecked);
        if (result != 0) {
            return ServerResponse.createdBySucessMessage("勾选成功");
        }
        return ServerResponse.createdByErrorMessage("勾选失败");
    }

    /**
     * 获取用户购物车的商品总数
     * @param userId 用户 ID
     * @return 总个数
     */
    public ServerResponse<Integer> getCartCount(Integer userId) {
        // 这里使用 mybatis，有个要注意的地方就是假如用户购物车没有商品，mybatis 返回的数值会为空，此时使用 int 类型会报错，解决方法有两种
        // 1、使用 Integer 类型
        // 2、在 sql 语句中使用如 IFNULL(count(1), 0) 声明默认值
        int result = cartMapper.getCartCount(userId);
        return ServerResponse.createdBySuccess(result);
    }

}
