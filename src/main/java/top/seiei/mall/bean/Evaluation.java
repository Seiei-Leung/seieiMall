package top.seiei.mall.bean;

import java.util.Date;

public class Evaluation {
    private Integer id;

    private Long orderNo;

    private Integer orderItemId;

    private Integer productId;

    private Integer userId;

    private Integer evaluationRank;

    private String content;

    private String newContent;

    private String businessContent;

    private String refundReason;

    private String exchangeReason;

    private Date madeNewContentTime;

    private Date businessContentTime;

    private Date createTime;

    private Date updateTime;

    public Evaluation(Integer id, Long orderNo, Integer orderItemId, Integer productId, Integer userId, Integer evaluationRank, String content, String newContent, String businessContent, String refundReason, String exchangeReason, Date madeNewContentTime, Date businessContentTime, Date createTime, Date updateTime) {
        this.id = id;
        this.orderNo = orderNo;
        this.orderItemId = orderItemId;
        this.productId = productId;
        this.userId = userId;
        this.evaluationRank = evaluationRank;
        this.content = content;
        this.newContent = newContent;
        this.businessContent = businessContent;
        this.refundReason = refundReason;
        this.exchangeReason = exchangeReason;
        this.madeNewContentTime = madeNewContentTime;
        this.businessContentTime = businessContentTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Evaluation() {
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

    public Integer getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Integer orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getEvaluationRank() {
        return evaluationRank;
    }

    public void setEvaluationRank(Integer evaluationRank) {
        this.evaluationRank = evaluationRank;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public String getNewContent() {
        return newContent;
    }

    public void setNewContent(String newContent) {
        this.newContent = newContent == null ? null : newContent.trim();
    }

    public String getBusinessContent() {
        return businessContent;
    }

    public void setBusinessContent(String businessContent) {
        this.businessContent = businessContent == null ? null : businessContent.trim();
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason == null ? null : refundReason.trim();
    }

    public String getExchangeReason() {
        return exchangeReason;
    }

    public void setExchangeReason(String exchangeReason) {
        this.exchangeReason = exchangeReason == null ? null : exchangeReason.trim();
    }

    public Date getMadeNewContentTime() {
        return madeNewContentTime;
    }

    public void setMadeNewContentTime(Date madeNewContentTime) {
        this.madeNewContentTime = madeNewContentTime;
    }

    public Date getBusinessContentTime() {
        return businessContentTime;
    }

    public void setBusinessContentTime(Date businessContentTime) {
        this.businessContentTime = businessContentTime;
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