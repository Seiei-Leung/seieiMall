package top.seiei.mall.controller.portal;

import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
    @RequestMapping(value = "add.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<Integer> add(HttpSession session, Shipping shipping) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户还没登录");
        }
        return iShippingService.add(user.getId(), shipping);
    }

    /**
     * 删除收货地址
     * @param session session 对象
     * @param shippingid 地址信息 ID
     * @return 是否删除成功
     */
    @RequestMapping("delete.do")
    @ResponseBody
    public ServerResponse delete(HttpSession session, Integer shippingid) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户还没登录");
        }
        return iShippingService.delete(user.getId(), shippingid);
    }

    /**
     * 更新地址信息
     * @param session session 对象
     * @param shipping 地址信息
     * @return 是否更新成功
     */
    @RequestMapping(value = "update.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse update(HttpSession session, Shipping shipping) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户还没登录");
        }
        return iShippingService.update(user.getId(), shipping);
    }

    /**
     * 获取一条收货地址详细信息
     * @param session session 对象
     * @param shippingid 收货地址 ID
     * @return 收货地址详细信息
     */
    @RequestMapping("select_one.do")
    @ResponseBody
    public ServerResponse<Shipping> selectOne(HttpSession session, Integer shippingid) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户还没登录");
        }
        return iShippingService.selectOne(user.getId(), shippingid);
    }

    /**
     * 根据用户 ID 获取所有收货地址列表
     * @param session session 对象
     * @param pageindex 结果列表初始页
     * @param pagesize 结果列表一页的容量
     * @return 所有收货地址列表
     */
    @RequestMapping("get_all.do")
    @ResponseBody
    public ServerResponse<PageInfo> selectAll(HttpSession session,
                                              @RequestParam(value = "pageindex", defaultValue = "1") Integer pageindex,
                                              @RequestParam(value = "pagesize", defaultValue = "10") Integer pagesize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户还没登录");
        }
        return iShippingService.selectAll(user.getId(), pageindex, pagesize);
    }
}
