package top.seiei.mall.controller.portal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    /**
     * 前台用户登录
     * @param userName
     * @param passWord
     * @return 返回用户信息
     */
    @RequestMapping(value="login.do", method= RequestMethod.POST)
    @ResponseBody
    public Object login(String userName, String passWord) {

        return null;
    }
}
