package top.seiei.mall.common;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

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

    // 购物车商品是否已勾选
    public interface Cart {
        boolean CHECKED = true;
        boolean UN_CHECKED = false;
    }

    // 排序类型
    // asc 表示升序
    // desc 表示降序
    public interface ProductListOrderBy {
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    // 商品状态枚举类
    public enum ProductStatusEnum {
        ON_SALE("on sale",1);

        private String value;
        private int code;
        ProductStatusEnum(String value, int code) {
            this.value = value;
            this.code = code;
        }
        public String getValue() {
            return value;
        }
        public int getCode() {
            return code;
        }
    }
}
