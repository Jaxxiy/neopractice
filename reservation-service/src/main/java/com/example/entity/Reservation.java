package com.example.entity;

import com.example.enums.ReservationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @Column(name = "id_reservation", columnDefinition = "UUID")
    private UUID idReservation;

    @Column(name = "id_product", nullable = false)
    private String idProduct;

    @Column(name = "count", nullable = false)
    private Integer count;

    @Column(name = "id_user", nullable = false)
    private String idUser;

    @Column(name = "date_create", nullable = false, updatable = false)
    private LocalDateTime dateCreate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    public Reservation() {
    }

    public Reservation(UUID idReservation, String idProduct, Integer count,
                       String idUser, LocalDateTime dateCreate, ReservationStatus status) {
        this.idReservation = idReservation;
        this.idProduct = idProduct;
        this.count = count;
        this.idUser = idUser;
        this.dateCreate = dateCreate;
        this.status = status;
    }

    public UUID getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(UUID idReservation) {
        this.idReservation = idReservation;
    }

    public String getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(String idProduct) {
        this.idProduct = idProduct;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public LocalDateTime getDateCreate() {
        return dateCreate;
    }

    public void setDateCreate(LocalDateTime dateCreate) {
        this.dateCreate = dateCreate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "idReservation=" + idReservation +
                ", idProduct='" + idProduct + '\'' +
                ", count=" + count +
                ", idUser='" + idUser + '\'' +
                ", dateCreate=" + dateCreate +
                ", status=" + status +
                '}';
    }
}