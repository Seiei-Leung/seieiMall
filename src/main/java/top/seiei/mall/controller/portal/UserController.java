package top.seiei.mall.controller.portal;

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
 *
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    // @Resource 是优先依据名称注入
    @Resource
    private IUserService iUserService;

    /**
     * 前台用户登录
     * POST 方法
     * @param userName 用户名
     * @param passWord 密码
     * @return 返回用户信息
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String userName, String passWord, HttpSession session) {
        ServerResponse<User> response =  iUserService.login(userName, passWord);
        // 如果登录成功，将用户信息添加到 Session 中
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    /**
     * 退出登录
     * GET 方法
     * @param session 当前用户的 session 对象
     * @return 响应对象
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session) {
        // 删除当前用户 session
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createdBySuccess();
    }

    /**
     * 登录注册
     * POST 方法
     * @param user 用户信息
     * @return 响应对象
     */
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return iUserService.register(user);
    }

    /**
     * 检验邮箱，用户名是否存在
     * GET 方法
     * @param value 邮箱或用户名字符串
     * @param type 类型，邮箱或用户名
     * @return 响应对象
     */
    @RequestMapping(value = "check_vaild.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> checkVaild(String value, String type) {
        return iUserService.checkVaild(value, type);
    }

    /**
     * 获取已经登录的用户消息，该消息在用户登录的时候，存储在该 Session 对象中
     * @param session 已经登录的用户 Session 对象
     * @return 响应对象（用户消息）
     */
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        // 在 session 中获取当前用户消息返回，而不是根据传入用户名查找这种方式
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            return ServerResponse.createdBySuccess(user);
        }
        return ServerResponse.createdByErrorMessage("用户未登录，无法获取用户消息");
    }

    /**
     * 未登录状态下，忘记密码获取密保问题
     * @param userName 用户名
     * @return 密保问题
     */
    @RequestMapping(value = "get_question.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> getQuestion(String userName) {
        return iUserService.getQuestion(userName);
    }

    /**
     * 提交密保答案并检验
     * @param userName 用户名
     * @param question 密保问题
     * @param answer 密保问题答案
     * @return 本地缓存令牌Token，修改密码时提交需要检验带上这个令牌 Token
     */
    @RequestMapping(value = "check_question.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> checkQuestion(String userName, String question, String answer) {
        return iUserService.checkQuestion(userName, question, answer);
    }

    /**
     * 修改密码
     * @param userName 用户名
     * @param newPassword 新密码
     * @param token token
     * @return 响应对象
     */
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(String userName, String newPassword, String token) {
        return iUserService.resetPassword(userName, newPassword, token);
    }
}
