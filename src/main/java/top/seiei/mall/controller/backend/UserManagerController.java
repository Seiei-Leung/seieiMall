package top.seiei.mall.controller.backend;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import top.seiei.mall.bean.User;
import top.seiei.mall.common.Const;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.service.IUserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * 管理员用户接口
 */
@Controller
@RequestMapping("/manager/user/")
public class UserManagerController {

    @Resource
    private IUserService iUserService;

    /**
     * 后台页面，管理员登录
     * @param userName 用户名
     * @param password 用户密码
     * @param session session 对象
     * @return 响应对象
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String userName, String password, HttpSession session) {
        ServerResponse<User> serverResponse = iUserService.checkAdmin(userName, password);
        if (serverResponse.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, serverResponse.getData());
        }
        return serverResponse;
    }
}
