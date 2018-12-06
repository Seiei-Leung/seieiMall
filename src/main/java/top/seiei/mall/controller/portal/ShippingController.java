package top.seiei.mall.controller.portal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import top.seiei.mall.bean.Shipping;
import top.seiei.mall.bean.User;
import top.seiei.mall.common.Const;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.service.IShippingService;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RequestMapping("/shipping/")
@Controller
public class ShippingController {

    @Resource
    private IShippingService iShippingService;

    /**
     * 新增收货地址
     * @param session session 对象
     * @param shipping 地址信息
     * @return 新增地址信息后返回的主键
     */
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse<Integer> add(HttpSession session, Shipping shipping) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户还没登录");
        }
        return iShippingService.add(user.getId(), shipping);
    }

}
