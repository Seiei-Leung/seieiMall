package top.seiei.mall.controller.portal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import top.seiei.mall.bean.User;
import top.seiei.mall.common.Const;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.service.impl.UserServiceImpl;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 *
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    @Resource
    private UserServiceImpl iUserService;

    /**
     * 前台用户登录
     * @param userName
     * @param passWord
     * @return 返回用户信息
     */
    @RequestMapping(value="login.do", method= RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String userName, String passWord, HttpSession session) {
        ServerResponse<User> response =  iUserService.login(userName, passWord);

        // 如果登录成功，将用户信息添加到 Session 中
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }

        return response;
    }
}
