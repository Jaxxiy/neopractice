package com.example.dto;

import com.example.enums.ReservationStatus;

import java.io.Serializable;

public class ReservationStatusMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderId;
    private ReservationStatus status;
    private String message;
    private Integer availableCount;
    private Long timestamp;

    public ReservationStatusMessage() {
    }

    public ReservationStatusMessage(String orderId, ReservationStatus status, String message) {
        this.orderId = orderId;
        this.status = status;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public ReservationStatusMessage(String orderId, ReservationStatus status, String message, Integer availableCount) {
        this.orderId = orderId;
        this.status = status;
        this.message = message;
        this.availableCount = availableCount;
        this.timestamp = System.currentTimeMillis();
    }

    public Integer getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(Integer availableCount) {
        this.availableCount = availableCount;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ReservationStatusMessage{" +
                "orderId='" + orderId + '\'' +
                ", status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}