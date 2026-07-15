package com.example.controller;

import com.example.dto.ReservationDetailsDTO;
import com.example.dto.ReservationRequestDTO;
import com.example.dto.ReservationResponseDTO;
import com.example.service.ReservationService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
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
    private final Tracer tracer;

    public ReservationController(ReservationService reservationService, Tracer tracer) {
        this.reservationService = reservationService;
        this.tracer = tracer;
    }

    @PostMapping
    @Operation(summary = "Create a new reservation")
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @Valid @RequestBody ReservationRequestDTO request) {
        log.info("Received reservation request for product: {}, count: {}",
                request.getIdProduct(), request.getCount());

        Span span = tracer.spanBuilder("POST /api/reservations")
                .setAttribute("product.id", request.getIdProduct())
                .setAttribute("product.count", request.getCount())
                .setAttribute("user.id", request.getIdUser())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            ReservationResponseDTO response = reservationService.createReservation(request);

            span.setAttribute("reservation.id", response.getReservationId());
            span.setAttribute("reservation.status", response.getStatus());
            span.setAttribute("operation.success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            span.setAttribute("operation.success", false);
            span.setAttribute("error", true);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @GetMapping
    @Operation(summary = "Get all reservations")
    public ResponseEntity<List<ReservationDetailsDTO>> getAllReservations() {
        log.info("Received request to get all reservations");

        Span span = tracer.spanBuilder("GET /api/reservations")
                .setAttribute("operation.type", "get_all")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            List<ReservationDetailsDTO> response = reservationService.getAllReservations();
            span.setAttribute("result.count", response.size());
            span.setAttribute("operation.success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            span.setAttribute("operation.success", false);
            span.setAttribute("error", true);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID")
    public ResponseEntity<ReservationDetailsDTO> getReservationById(
            @Parameter(description = "Reservation UUID") @PathVariable UUID id) {
        log.info("Received request to get reservation by id: {}", id);

        Span span = tracer.spanBuilder("GET /api/reservations/{id}")
                .setAttribute("reservation.id", id.toString())
                .setAttribute("operation.type", "get_by_id")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            ReservationDetailsDTO response = reservationService.getReservationById(id);
            span.setAttribute("operation.success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            span.setAttribute("operation.success", false);
            span.setAttribute("error", true);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get reservations by user ID")
    public ResponseEntity<List<ReservationDetailsDTO>> getReservationsByUser(
            @Parameter(description = "User ID") @PathVariable String userId) {
        log.info("Received request to get reservations for user: {}", userId);

        Span span = tracer.spanBuilder("GET /api/reservations/user/{userId}")
                .setAttribute("user.id", userId)
                .setAttribute("operation.type", "get_by_user")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            List<ReservationDetailsDTO> response = reservationService.getReservationsByUser(userId);
            span.setAttribute("result.count", response.size());
            span.setAttribute("operation.success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            span.setAttribute("operation.success", false);
            span.setAttribute("error", true);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get reservations by product ID")
    public ResponseEntity<List<ReservationDetailsDTO>> getReservationsByProduct(
            @Parameter(description = "Product ID") @PathVariable String productId) {
        log.info("Received request to get reservations for product: {}", productId);

        Span span = tracer.spanBuilder("GET /api/reservations/product/{productId}")
                .setAttribute("product.id", productId)
                .setAttribute("operation.type", "get_by_product")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            List<ReservationDetailsDTO> response = reservationService.getReservationsByProduct(productId);
            span.setAttribute("result.count", response.size());
            span.setAttribute("operation.success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            span.setAttribute("operation.success", false);
            span.setAttribute("error", true);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a reservation")
    public ResponseEntity<Void> cancelReservation(
            @Parameter(description = "Reservation UUID") @PathVariable UUID id) {
        log.info("Received request to cancel reservation: {}", id);

        Span span = tracer.spanBuilder("DELETE /api/reservations/{id}")
                .setAttribute("reservation.id", id.toString())
                .setAttribute("operation.type", "cancel")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            reservationService.cancelReservation(id);
            span.setAttribute("operation.success", true);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            span.setAttribute("operation.success", false);
            span.setAttribute("error", true);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}