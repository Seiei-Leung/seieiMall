package top.seiei.mall.common;

import java.io.Serializable;

/**
 * 高复用服务响应对象，它需要实现序列化接口
 * 它有三个属性，分别是：status（状态码，非零代表不成功），msg（错误状态码解释），data（返回数据）
 * 泛型 T 是 返回数据类型
 */
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
    public boolean isSuccess() {
        return this.status == ResponseCode.SUCCESS.getCode();
    }


    public static void main(String[] args) {
        ServerResponse serverResponse = new ServerResponse(1, "2");
    }


}
