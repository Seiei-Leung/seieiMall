package top.seiei.mall.service;

import top.seiei.mall.bean.User;
import top.seiei.mall.common.ServerResponse;

public interface IUserService {

    ServerResponse<User> login(String userName, String passWord);

    ServerResponse<String> register(User user);

    ServerResponse<String> checkVaild(String value, String type);

    ServerResponse<String> getQuestion(String userName);

    ServerResponse<String> checkQuestion(String userName, String question, String answer);

    public ServerResponse<String> resetPassword(String userName, String newPassword, String token);
}
