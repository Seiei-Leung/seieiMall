package top.seiei.mall.controller.portal;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import top.seiei.mall.bean.User;
import top.seiei.mall.common.Const;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.service.ICartService;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/cart/")
public class CartController {

    @Resource
    private ICartService iCartService;

    /**
     * 获取用户购物车所有信息
     * @param session session 对象
     * @return 响应对象
     */
    @RequestMapping("get_cart_list")
    @ResponseBody
    public ServerResponse getCartList(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户还没登录");
        }

        return null;
    }

    /**
     * 用户添加商品到购物车或修改购物车商品的数量或勾选
     * @param session session 对象
     * @param productid 商品id
     * @param count 商品个数
     * @param ischecked 是否已经勾选
     * @return 响应对象
     */
    @RequestMapping("addorupdate_product.do")
    @ResponseBody
    public ServerResponse addOrUpdateProduct(HttpSession session, Integer productid, Integer count, @RequestParam(value = "ischecked", defaultValue = "true") Boolean ischecked) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户还没登录");
        }
        return iCartService.addOrUpdateProduct(user.getId(), productid, count, ischecked);
    }

    /**
     * 购物车批量删除商品
     * @param session session 对象
     * @param productidlist 商品id 数组
     * @return 响应对象
     */
    @RequestMapping("delete_product.do")
    @ResponseBody
    public ServerResponse deleteProduct(HttpSession session, List<Integer> productidlist) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户还没登录");
        }
        return iCartService.deleteProduct(user.getId(), productidlist);
    }






}
