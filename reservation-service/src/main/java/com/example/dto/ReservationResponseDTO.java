package com.example.dto;

public class ReservationResponseDTO {
    private String status;
    private String reservationId;
    private String message;

    public ReservationResponseDTO() {
    }

    public ReservationResponseDTO(String status, String reservationId, String message) {
        this.status = status;
        this.reservationId = reservationId;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ReservationResponseDto{" +
                "status='" + status + '\'' +
                ", reservationId='" + reservationId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
