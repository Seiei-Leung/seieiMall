package top.seiei.mall.common;

/**
 * 常量类
 */
public class Const {
    public static final String CURRENT_USER = "currentUser"; // 当前用户
    public static final String USERNAME = "userName"; // 用于选择查重类型，类型为用户名
    public static final String EMAIL = "email"; // 用于选择查重类型，类型为用户名

    // 内部定义接口的好处：具体可以理解成一个类中进一步的逻辑细分
    public interface Role {
        int ROLE_CUSTOMER = 0; // 普通用户
        int ROLE_ADMIN = 1; // 管理员
    }
}
