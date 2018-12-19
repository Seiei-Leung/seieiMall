package top.seiei.mall.controller.backend;

import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import top.seiei.mall.bean.User;
import top.seiei.mall.common.Const;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.service.IOrderService;
import top.seiei.mall.service.IUserService;
import top.seiei.mall.vo.OrderVo;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;

@Controller
@RequestMapping("/manage/order/")
public class OrderManagerController {

    @Resource
    private IUserService iUserService;

    @Resource
    private IOrderService iOrderService;

    /**
     * 后台接口，获取所有订单（分页）
     * @param session session 对象
     * @param pageindex 初始页数
     * @param pagesize 一页容量
     * @return
     */
    @RequestMapping("get_all_order.do")
    @ResponseBody
    public ServerResponse<PageInfo> getAllOrder(HttpSession session,
                                                        @RequestParam(value = "pageindex", defaultValue = "1") int pageindex,
                                                        @RequestParam(value = "pagesize", defaultValue = "10") int pagesize) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iOrderService.getAllOrderOfManage(pageindex, pagesize);
    }

    /**
     * 后台接口，根据订单号获取订单详情
     * @param session session 对象
     * @param orderno 订单号
     * @return
     */
    @RequestMapping("get_by_orderno.do")
    @ResponseBody
    public ServerResponse<OrderVo> getByOrderNo(HttpSession session, Long orderno) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iOrderService.getByOrderNoOfManage(orderno);
    }

    /**
     * 后端接口，商品发货
     * @param session session 对象
     * @param orderno 订单号
     * @param expressno 快递单号
     * @param expresscompany 快递公司
     * @return
     */
    @RequestMapping("sendGoods.do")
    @ResponseBody
    public ServerResponse sendGoods(HttpSession session,
                                    Long orderno,
                                    Long expressno,
                                    String expresscompany,
                                    @RequestParam(value = "expresspay", defaultValue = "0") BigDecimal expresspay) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iOrderService.sendGoods(orderno, expressno, expresscompany, expresspay);
    }

    /**
     * 获取所有退款申请的订单
     * @param session session 对象
     * @param pageindex 初始页
     * @param pagesize 一页容量
     * @return OrderVo 集合
     */
    @RequestMapping("get_all_refund_order.do")
    @ResponseBody
    public ServerResponse<PageInfo> getALLRefundOrder(HttpSession session,
                                                      @RequestParam(value = "pageindex", defaultValue = "1") int pageindex,
                                                      @RequestParam(value = "pagesize", defaultValue = "10") int pagesize) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iOrderService.getAllRefundOrder(pageindex, pagesize);
    }

    /**
     * 获取所有换货申请的订单
     * @param session session 对象
     * @param pageindex 初始页
     * @param pagesize 一页容量
     * @return OrderVo 集合
     */
    @RequestMapping("get_all_exchange_order.do")
    @ResponseBody
    public ServerResponse<PageInfo> getAllExchangeOrder(HttpSession session,
                                                      @RequestParam(value = "pageindex", defaultValue = "1") int pageindex,
                                                      @RequestParam(value = "pagesize", defaultValue = "10") int pagesize) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iOrderService.getAllExchangeOrder(pageindex, pagesize);
    }

    // TODO 退款确认，接入支付宝
}
