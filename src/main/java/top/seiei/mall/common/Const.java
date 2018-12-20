package top.seiei.mall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 常量类
 */
public class Const {
    public static final String CURRENT_USER = "currentUser"; // 当前用户
    public static final String USERNAME = "userName"; // 用于选择查重类型，类型为用户名
    public static final String EMAIL = "email"; // 用于选择查重类型，类型为用户名
    public static final int TRADE_DEADLINE = 7; // 确认交易期限，自动设置交易成功，并且此后不能退货退款

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

    // 支付订单的状态
    public enum OrderStatusEnum {
        CANCEL("已取消", 0),
        NO_PAY("未支付", 10),
        PAID("已支付", 20),
        SHIPPED("已发货", 30),
        APPLY_REFUND("申请退款", 40), // 申请退款
        APPLY_EXCHANGE_GOOD("申请换货", 50), // 申请换货
        EXCHANGING_GOOD("换货中", 51), // 换货中
        ORDER_SUCCESS("订单完成", 60), // 正常订单完成
        NO_REFUND("订单完成", 61), // 不允许退款情况下，订单完成
        EXCHANGED_GOOD("订单完成", 62), // 允许换货情况下，订单完成
        NO_EXCHANGE_GOOD("订单完成", 63), // 不允许换货情况下，订单完成
        ORDER_CLOSE("订单关闭", 70), // 正常情况下，订单关闭，比如付款时间过期
        REFUNDED("订单关闭", 71); // 退款后，订单关闭

        private String value;
        private int code;
        OrderStatusEnum(String value, int code) {
            this.value = value;
            this.code = code;
        }
        public String getValue() {
            return value;
        }
        public int getCode() {
            return code;
        }
        public static OrderStatusEnum codeOf(Integer code) {
            for (OrderStatusEnum item : values()) {
                if (item.getCode() == code) {
                    return item;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }

    // 支付宝回调所用的常量类，包括回调状态以及返回回调接口的字符串格式
    public interface AlipayCallback {
        String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_SUCCESS = "TRADE_SUCCESS";
        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    // 支付平台
    public enum PayPlatformEnum {
        ALIPAY(1,"支付宝");

        PayPlatformEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;
        public String getValue() {
            return value;
        }
        public int getCode() {
            return code;
        }
    }

    // 支付方式，如在线支付、货到付款
    public enum PaymentTypeEnum {
        ONLINE_PAY(1, "在线支付");

        PaymentTypeEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        private String value;
        private int code;
        public String getValue() {
            return value;
        }
        public int getCode() {
            return code;
        }
        public static PaymentTypeEnum codeOf(Integer code) {
            for (PaymentTypeEnum item : values()) {
                if (item.getCode() == code) {
                    return item;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }


}
