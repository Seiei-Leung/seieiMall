package top.seiei.mall.vo;

import java.math.BigDecimal;
import java.util.Date;

public class ExpressVo {

    private Long orderNo;

    private Long expressNo;

    private String expressCompany;

    private BigDecimal expressPay;

    private Date createTime;

    public Long getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Long orderNo) {
        this.orderNo = orderNo;
    }

    public Long getExpressNo() {
        return expressNo;
    }

    public void setExpressNo(Long expressNo) {
        this.expressNo = expressNo;
    }

    public String getExpressCompany() {
        return expressCompany;
    }

    public void setExpressCompany(String expressCompany) {
        this.expressCompany = expressCompany;
    }

    public BigDecimal getExpressPay() {
        return expressPay;
    }

    public void setExpressPay(BigDecimal expressPay) {
        this.expressPay = expressPay;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
