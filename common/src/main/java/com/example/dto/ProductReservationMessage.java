package com.example.dto;

import java.io.Serializable;

public class ProductReservationMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;
    private Integer count;
    private String orderId;
    private String idUser;

    public ProductReservationMessage() {
    }

    public ProductReservationMessage(String productId, Integer count, String orderId) {
        this.productId = productId;
        this.count = count;
        this.orderId = orderId;
    }

    public ProductReservationMessage(String productId, Integer count, String orderId, String idUser) {
        this.productId = productId;
        this.count = count;
        this.orderId = orderId;
        this.idUser = idUser;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
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
