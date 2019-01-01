package top.seiei.mall.controller.backend;

import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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
import java.util.List;

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

    // TODO 此时还要获取商品评论以及如果商品为退货或退货状态，要携带出其原因
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

    // TODO 连接快递信息接口，当快递状态是已收货的时候，回调成功接口，设置确认收货最晚期限
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
        return iOrderService.sendGoodsByManage(orderno, expressno, expresscompany, expresspay);
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
        return iOrderService.getAllRefundOrderByManage(pageindex, pagesize);
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
        return iOrderService.getAllExchangeOrderByManage(pageindex, pagesize);
    }

    /**
     * 后台，确认退款
     * @param session session 对象
     * @param orderno 订单号
     * @param orderitemidlist 子订单 ID 集合，以逗号间隔的字符串形式表示
     * @return 是否成功
     */
    @RequestMapping(value = "refund.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse refund(HttpSession session, Long orderno, String orderitemidlist) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iOrderService.refundByManage(orderno, orderitemidlist);
    }

    /**
     * 后台，确认换货
     * @param session session 对象
     * @param orderno 订单号
     * @param orderitemidlist 子订单 ID 集合，以逗号间隔的字符串形式表示
     * @return 是否成功
     */
    @RequestMapping(value = "exchange_good.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse exchangeGoods(HttpSession session, Long orderno, String orderitemidlist) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iOrderService.exchangeGoodByManage(orderno, orderitemidlist);
    }

    // TODO 强制关闭交易时，该订单的子订单是否也全部强制关闭，如果不是，那么这些子订单的订单状态如何确认，父订单的订单状态是否真的有必要存在理由
    /**
     * 强制确认交易完成、关闭
     * @param session session 对象
     * @param ordernolist 订单号集合
     * @return 是否成功
     */
    @RequestMapping(value = "close_orders.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse completeOrder(HttpSession session, @RequestBody List<Long> ordernolist) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iOrderService.completeOrderByManage(ordernolist);
    }


}
