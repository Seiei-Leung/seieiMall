package top.seiei.mall.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import top.seiei.mall.bean.User;
import top.seiei.mall.common.ServerResponse;
import top.seiei.mall.dao.UserMapper;
import top.seiei.mall.service.IUserService;

import javax.annotation.Resource;

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
        int resultCount = userMapper.checkByUserName(userName);
        if (resultCount == 0) {
            return  ServerResponse.createdByErrorMessage("该用户不存在");
        }

        //todo 密码转成MD5

        // 获取用户信息
        User user = userMapper.selectForLogin(userName, passWord);
        // 密码错误
        if (user == null) {
            return ServerResponse.createdByErrorMessage("密码错误");
        }
        // 设置空密码
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createdBySuccess("登录成功", user);
    }
}
