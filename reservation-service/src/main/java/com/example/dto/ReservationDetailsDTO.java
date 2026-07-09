package com.example.dto;

import com.example.enums.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReservationDetailsDTO {

        private UUID idReservation;
        private String idProduct;
        private Integer count;
        private String idUser;
        private LocalDateTime dateCreate;
        private ReservationStatus status;

        public ReservationDetailsDTO() {
        }

        public ReservationDetailsDTO(UUID idReservation, String idProduct, Integer count,
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
            return "ReservationDetailsDto{" +
                    "idReservation=" + idReservation +
                    ", idProduct='" + idProduct + '\'' +
                    ", count=" + count +
                    ", idUser='" + idUser + '\'' +
                    ", dateCreate=" + dateCreate +
                    ", status=" + status +
                    '}';
        }
    }

