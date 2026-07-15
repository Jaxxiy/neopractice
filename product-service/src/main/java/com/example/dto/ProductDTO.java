package com.example.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProductDTO {
    private UUID idProduct;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotNull(message = "Count is required")
    @Min(value = 0, message = "Count must be at least 0")
    private Integer count;

    private LocalDateTime dateTimeLastChange;

    public LocalDateTime getDateTimeLastChange() {
        return dateTimeLastChange;
    }

    public void setDateTimeLastChange(LocalDateTime dateTimeLastChange) {
        this.dateTimeLastChange = dateTimeLastChange;
    }

    public ProductDTO() {
    }

    public ProductDTO(UUID idProduct, String name, Integer count) {
        this.idProduct = idProduct;
        this.name = name;
        this.count = count;
    }

    public UUID getIdProduct() {
        return idProduct;
    }

    public void setIdProduct(UUID idProduct) {
        this.idProduct = idProduct;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "ProductDto{" +
                "idProduct=" + idProduct +
                ", name='" + name + '\'' +
                ", count=" + count +
                '}';
    }
}