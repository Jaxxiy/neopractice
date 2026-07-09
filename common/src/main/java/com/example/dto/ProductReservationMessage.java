package com.example.dto;

import java.io.Serializable;

public class ProductReservationMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;
    private Integer count;
    private String orderId;

    public ProductReservationMessage() {
    }

    public ProductReservationMessage(String productId, Integer count, String orderId) {
        this.productId = productId;
        this.count = count;
        this.orderId = orderId;
    }


    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "ProductReservationMessage{" +
                "productId='" + productId + '\'' +
                ", count=" + count +
                ", orderId='" + orderId + '\'' +
                '}';
    }
}
