package com.example.dto;

import com.example.enums.ReservationStatus;

import java.io.Serializable;

public class ReservationStatusMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderId;
    private ReservationStatus status;
    private String message;

    public ReservationStatusMessage() {
    }

    public ReservationStatusMessage(String orderId, ReservationStatus status, String message) {
        this.orderId = orderId;
        this.status = status;
        this.message = message;
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