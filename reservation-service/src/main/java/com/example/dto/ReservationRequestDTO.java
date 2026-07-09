package com.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReservationRequestDTO {

    @NotBlank(message = "Product ID is required")
    private String idProduct;

    @NotNull(message = "Count is required")
    @Min(value = 1, message = "Count must be at least 1")
    private Integer count;

    @NotBlank(message = "User ID is required")
    private String idUser;

    public ReservationRequestDTO() {
    }

    public ReservationRequestDTO(String idProduct, Integer count, String idUser) {
        this.idProduct = idProduct;
        this.count = count;
        this.idUser = idUser;
    }

    public @NotBlank(message = "Product ID is required") String getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(@NotBlank(message = "Product ID is required") String idProduct) {
        this.idProduct = idProduct;
    }

    public @NotNull(message = "Count is required") @Min(value = 1, message = "Count must be at least 1") Integer getCount() {
        return count;
    }

    public void setCount(@NotNull(message = "Count is required") @Min(value = 1, message = "Count must be at least 1") Integer count) {
        this.count = count;
    }

    public @NotBlank(message = "User ID is required") String getIdUser() {
        return idUser;
    }

    public void setIdUser(@NotBlank(message = "User ID is required") String idUser) {
        this.idUser = idUser;
    }

    @Override
    public String toString() {
        return "ReservationRequestDto{" +
                "productId='" + idProduct + '\'' +
                ", count=" + count +
                ", userId='" + idUser + '\'' +
                '}';
    }
}