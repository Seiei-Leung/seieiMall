package top.seiei.mall.controller.backend;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import top.seiei.mall.bean.Product;
import top.seiei.mall.bean.User;
import top.seiei.mall.common.Const;
import top.seiei.mall.common.ResponseCode;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.service.IProductService;
import top.seiei.mall.service.IUserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/product/")
public class ProductManagerController {

    @Resource
    private IUserService iUserService;

    @Resource
    private IProductService iProductService;

    /**
     * 新增或修改商品信息
     * @param session session 对象
     * @param product 商品信息
     * @return 响应对象
     */
    @RequestMapping("save_product.do")
    @ResponseBody
    public ServerResponse<String> saveProduct(HttpSession session, Product product) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iProductService.saveProduct(product);
    }

    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse<String> setSaleStatus(HttpSession session, Integer id, Integer status) {
        // 首先检查是否为管理员
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createdByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
        }
        ServerResponse<String> serverResponse = iUserService.checkAdmin(user);
        if (!serverResponse.isSuccess()) {
            return ServerResponse.createdByErrorMessage("该用户不是管理员，无权限操作");
        }
        return iProductService.setSaleStatus(id, status);
    }

}
