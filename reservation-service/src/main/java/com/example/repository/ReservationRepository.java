package com.example.repository;

import com.example.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByStatus(String status);

    List<Reservation> findByIdUser(String userId);

    List<Reservation> findByIdProduct(String productId);

    List<Reservation> findByDateCreateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM Reservation r WHERE r.idProduct = :productId ORDER BY r.dateCreate DESC")
    List<Reservation> findLatestByProductId(@Param("productId") String productId);

    boolean existsByIdReservationAndStatus(UUID id, String status);
}