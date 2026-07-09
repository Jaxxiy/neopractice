package com.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(name = "id_product", columnDefinition = "UUID")
    private UUID idProduct;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "count", nullable = false)
    private Integer count;

    @Column(name = "date_time_last_change", nullable = false)
    private LocalDateTime dateTimeLastChange;

    public Product() {
    }

    public Product(UUID idProduct, String name, Integer count, LocalDateTime dateTimeLastChange) {
        this.idProduct = idProduct;
        this.name = name;
        this.count = count;
        this.dateTimeLastChange = dateTimeLastChange;
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

    public LocalDateTime getDateTimeLastChange() {
        return dateTimeLastChange;
    }

    public void setDateTimeLastChange(LocalDateTime dateTimeLastChange) {
        this.dateTimeLastChange = dateTimeLastChange;
    }

    @Override
    public String toString() {
        return "Product{" +
                "idProduct=" + idProduct +
                ", name='" + name + '\'' +
                ", count=" + count +
                ", dateTimeLastChange=" + dateTimeLastChange +
                '}';
    }
}
