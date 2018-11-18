package top.seiei.mall.common;

/**
 * 响应状态码枚举对象
 * 有两个属性，分别是：code（状态码），desc（描述）
 */
public enum ResponseCode {

    // 枚举对象实例
    SUCCESS(0, "SUCCESS"),
    ERROR(1, "ERROR"),
    ILLEGAL_ARGUMENT(2, "ILLEGAL_ARGUMENT"),
    NEED_LOGIN(10, "NEED_LOGIN");

    // 构造函数
    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private final int code;
    private final String desc;

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
