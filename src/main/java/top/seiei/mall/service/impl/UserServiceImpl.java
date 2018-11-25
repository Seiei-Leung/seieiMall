package top.seiei.mall.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import top.seiei.mall.bean.User;
import top.seiei.mall.common.Const;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.common.TokenCach;
import top.seiei.mall.dao.UserMapper;
import top.seiei.mall.service.IUserService;
import top.seiei.mall.util.MD5Util;

import javax.annotation.Resource;
import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 获取登录信息
     * @param userName 用户名
     * @param passWord 用户密码
     * @return 响应对象
     */
    @Override
    public ServerResponse<User> login(String userName, String passWord) {
        // 是否有该用户
        int resultCount = userMapper.checkUserName(userName);
        if (resultCount == 0) {
            return  ServerResponse.createdByErrorMessage("该用户不存在");
        }
        // MD5加密
        passWord = MD5Util.MD5EncodeUtf8(passWord);
        // 获取用户信息
        User user = userMapper.selectForLogin(userName, passWord);
        // 密码错误
        if (user == null) {
            return ServerResponse.createdByErrorMessage("密码错误");
        }
        // 设置空密码，使用的是 StringUtils.EMPTY
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createdBySuccess("登录成功", user);
    }

    /**
     * 注册用户
     * @param user 用户信息
     * @return 响应对象
     */
    @Override
    public ServerResponse<String> register(User user) {
        // 查重用户名
        ServerResponse<String> checkVaildResponse = checkVaild(user.getUsername(), Const.USERNAME);
        if (!checkVaildResponse.isSuccess()) {
            return  checkVaildResponse;
        }
        // 查重邮箱
        checkVaildResponse = checkVaild(user.getEmail(), Const.EMAIL);
        if (!checkVaildResponse.isSuccess()) {
            return  checkVaildResponse;
        }
        // 设置权限
        user.setRole(Const.Role.ROLE_ADMIN);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return  ServerResponse.createdByErrorMessage("注册失败");
        }
        return ServerResponse.createdBySucessMessage("注册成功");
    }

    /**
     * 查重用户名或邮箱，不重复返回成功
     * @param value 用户名或邮箱字符串
     * @param type 查重类型，用户名或邮箱
     * @return 响应对象
     */
    @Override
    public ServerResponse<String> checkVaild(String value, String type) {
        // 检测字符串是否为空
        if (StringUtils.isNotBlank(type)) {
            if (type.equals(Const.USERNAME)) {
                // 是否有该用户
                int resultCount = userMapper.checkUserName(value);
                if (resultCount > 0) {
                    return  ServerResponse.createdByErrorMessage("该用户已存在");
                }
            }
            else if (type.equals(Const.EMAIL)) {
                // 是否有该邮箱
                int resultCount = userMapper.checkEmail(value);
                if (resultCount > 0) {
                    return  ServerResponse.createdByErrorMessage("该邮箱已存在");
                }
            }
            else {
                // type 类型传入的值不是邮箱或用户名
                return ServerResponse.createdByErrorMessage("type 参数错误");
            }
        } else {
            return ServerResponse.createdByErrorMessage("type 参数错误");
        }
        return ServerResponse.createdBySucessMessage("检验成功");
    }

    /**
     * 忘记密码获取密保问题
     * @param userName 用户名
     * @return 密保问题
     */
    public ServerResponse<String> getQuestion(String userName) {
        // 查重用户名
        ServerResponse<String> checkVaildResponse = checkVaild(userName, Const.USERNAME);
        if (checkVaildResponse.isSuccess()) {
            return  ServerResponse.createdByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUserName(userName);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createdBySuccess(question);
        }
        return ServerResponse.createdByErrorMessage("用户的密保问题不存在");
    }

    /**
     * 提交密保答案并检验
     * @param userName 用户名
     * @param question 密保问题
     * @param answer 密保问题答案
     * @return 本地缓存令牌Token，修改密码时提交需要检验带上这个令牌 Token
     */
    public ServerResponse<String> checkQuestion(String userName, String question, String answer) {
        int resultCount = userMapper.checkByQuestion(userName, question, answer);
        // 验证成功
        if (resultCount > 0) {
            // 制作本地缓存令牌Token
            String forgetToken = UUID.randomUUID().toString();
            TokenCach.setKey(TokenCach.TOKENNAME_PREFIX + userName, forgetToken);
            // 返回 UUID,下次检验时，用户带来这个 UUID 与本地缓存中存储的 UUID 进行比较
            return ServerResponse.createdBySuccess(forgetToken);
        }
        return ServerResponse.createdByErrorMessage("密保答案错误");
    }

    /**
     * 忘记密码的状况下，修改密码
     * @param userName 用户名
     * @param newPassword 新密码
     * @param token token
     * @return 响应对象
     */
    public ServerResponse<String> forgetResetPassword(String userName, String newPassword, String token) {
        // 检验传进来的 token 是否为空
        if (StringUtils.isNotBlank(token)) {
            return ServerResponse.createdByErrorMessage("token 错误");
        }
        // 比较本地缓存中的 Token
        if (StringUtils.equals(token, TokenCach.getKey(TokenCach.TOKENNAME_PREFIX + userName))) {
            String MD5Password = MD5Util.MD5EncodeUtf8(newPassword);
            int resultCount = userMapper.updatePassword(userName, MD5Password);
            if (resultCount > 0) {
                return ServerResponse.createdBySucessMessage("修改密码成功");
            }
            return ServerResponse.createdByErrorMessage("修改密码失败");
        }
        return ServerResponse.createdByErrorMessage("token 错误或过期失效");
    }

    /**
     * 登录状态下，修改密码
     * @param user 当前登录用户的信息
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 响应对象
     */
    public ServerResponse<String> resetPassword(User user, String oldPassword, String newPassword) {
        // 检验用户的旧密码是否正确
        int resultCount = userMapper.checkPassword(user.getUsername(), MD5Util.MD5EncodeUtf8(oldPassword));
        if (resultCount == 0) {
            return ServerResponse.createdByErrorMessage("旧密码错误");
        }
        resultCount = userMapper.updatePassword(user.getUsername(), MD5Util.MD5EncodeUtf8(newPassword));
        if (resultCount > 0) {
            return ServerResponse.createdBySucessMessage("修改密码成功");
        }
        return ServerResponse.createdByErrorMessage("修改密码失败");
    }

    /**
     * 登录状态下，更新数据
     * @param user 用户更新信息
     * @return 响应对象
     */
    public ServerResponse<User> updateInformation(User user) {
        // userName 不能被修改
        // emaill 要认证
        // 假如用户没有填入 email，此时这个方法会返回什么？？
        int resultCount = userMapper.checkEmailByID(user.getId(), user.getEmail());
        if (resultCount > 0) {
            return  ServerResponse.createdByErrorMessage("Email已存在");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        resultCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (resultCount > 0) {
            return ServerResponse.createdBySuccess("更新个人信息成功", updateUser);
        }
        return ServerResponse.createdByErrorMessage("更新个人信息失败");
    }

    public ServerResponse<User> checkAdmin(String userName, String password) {
        ServerResponse<User> serverResponse = login(userName, password);
        if (serverResponse.isSuccess()) {
            if (serverResponse.getData().getRole() != Const.Role.ROLE_ADMIN) {
                return ServerResponse.createdByErrorMessage("该用户不是管理员");
            }
        }
        return serverResponse;
    }
}
