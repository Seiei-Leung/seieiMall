package top.seiei.mall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * 高复用服务响应对象，它需要实现序列化接口
 * 它有三个属性，分别是：status（响应状态码，非零代表不成功），msg（错误状态码解释），data（返回数据）
 * 泛型 T 是 返回数据类型
 */
// 当数值为 null 时，不返回键值对
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {

    private int status;
    private String msg;
    private T data;

    /**
     * 构造方法
     */
    // status 赋值
    private ServerResponse(int status) {
        this.status = status;
    }
    // status, msg 赋值
    private ServerResponse(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }
    // status，data 赋值
    private ServerResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }
    // status，msg, data 赋值
    private ServerResponse(int status,String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 外部访问该响应对象是否是一个成功的响应
     * @return 是否一个成功的响应
     */
    // 添加该注释，JSON序列化后不会显示在 JSON 中
    @JsonIgnore
    public boolean isSuccess() {
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
    public static <T> ServerResponse<T> createdBySuccess() {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    /**
     * 之所以，方法名称不是 createdBySuccess 是为了避免创建包含 data, status 的响应对象且 data 为 String 类型的方法，
     * 与创建包含 status, msg 的响应对象的方法发生冲突
     * @param msg 状态码解释
     * @return 响应对象
     */
    public static <T> ServerResponse<T> createdBySucessMessage(String msg) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg);
    }

    public static <T> ServerResponse<T> createdBySuccess(T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), data);
    }

    public static <T> ServerResponse<T> createdBySuccess(String msg, T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    /**
     * 最普通的错误响应对象，直接返回 "ERROR" 作为错误码解释
     */
    public static <T> ServerResponse<T> createdByError() {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getDesc());
    }

    public static <T> ServerResponse<T> createdByErrorMessage(String msg) {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(), msg);
    }

    /**
     * 用于创建其他错误响应状态码的响应对象
     * @param statusCode 错误响应状态码
     * @param msg 错误响应状态码解释
     * @return 响应对象
     */
    public static <T> ServerResponse<T> createdByErrorCodeMessage(int statusCode, String msg) {
        return  new ServerResponse<T>(statusCode, msg);
    }

    /**
     * 测试当泛型 data 类型为 String 时， 传入构造函数时，会调用那个构造函数
     * 测试结果为 调用参数为 status, msg 那个构造函数
     */
    public static void main(String[] args) {
        ServerResponse serverResponse = new ServerResponse(1, "2");
    }

}
