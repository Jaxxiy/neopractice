package com.example.controller;

import com.example.dto.ReservationDetailsDTO;
import com.example.dto.ReservationRequestDTO;
import com.example.dto.ReservationResponseDTO;
import com.example.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservation Service", description = "API for managing product reservations")
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @Operation(summary = "Create a new reservation")
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @Valid @RequestBody ReservationRequestDTO request) {
        log.info("Received reservation request for product: {}, count: {}",
                request.getIdProduct(), request.getCount());

        ReservationResponseDTO response = reservationService.createReservation(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all reservations")
    public ResponseEntity<List<ReservationDetailsDTO>> getAllReservations() {
        log.info("Received request to get all reservations");
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID")
    public ResponseEntity<ReservationDetailsDTO> getReservationById(
            @Parameter(description = "Reservation UUID") @PathVariable UUID id) {
        log.info("Received request to get reservation by id: {}", id);
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reservations by user ID")
    public ResponseEntity<List<ReservationDetailsDTO>> getReservationsByUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.info("Received request to get reservations for user: {}", userId);
        return ResponseEntity.ok(reservationService.getReservationsByUser(userId));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get reservations by product ID")
    public ResponseEntity<List<ReservationDetailsDTO>> getReservationsByProduct(
            @Parameter(description = "Product ID") @PathVariable String productId) {
        log.info("Received request to get reservations for product: {}", productId);
        return ResponseEntity.ok(reservationService.getReservationsByProduct(productId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a reservation")
    public ResponseEntity<Void> cancelReservation(
            @Parameter(description = "Reservation UUID") @PathVariable UUID id) {
        log.info("Received request to cancel reservation: {}", id);
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }
}