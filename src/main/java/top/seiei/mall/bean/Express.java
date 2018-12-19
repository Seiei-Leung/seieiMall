package top.seiei.mall.bean;

import java.math.BigDecimal;
import java.util.Date;

public class Express {
    private Integer id;

    private Long orderNo;

    private Long expressNo;

    private String expressCompany;

    private BigDecimal expressPay;

    private Date createTime;

    private Date updateTime;

    public Express(Integer id, Long orderNo, Long expressNo, String expressCompany, BigDecimal expressPay, Date createTime, Date updateTime) {
        this.id = id;
        this.orderNo = orderNo;
        this.expressNo = expressNo;
        this.expressCompany = expressCompany;
        this.expressPay = expressPay;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Express() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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
        this.expressCompany = expressCompany == null ? null : expressCompany.trim();
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}