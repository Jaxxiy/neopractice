package com.example.service;

import com.example.dto.ReservationStatusMessage;
import com.example.entity.Reservation;
import com.example.enums.ReservationStatus;
import com.example.repository.ReservationRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReservationResponseListener {

    private static final Logger log = LoggerFactory.getLogger(ReservationResponseListener.class);

    private final ReservationRepository reservationRepository;
    private final Tracer tracer;

    public ReservationResponseListener(ReservationRepository reservationRepository,
                                       Tracer tracer) {
        this.reservationRepository = reservationRepository;
        this.tracer = tracer;
    }
    
    @RabbitListener(queues = "${rabbitmq.queue.response:order.response.queue}")
    @Transactional
    public void handleReservationResponse(ReservationStatusMessage response) {
        log.info("=== RECEIVED RESPONSE FROM PRODUCT SERVICE ===");
        log.info("Response: orderId={}, status={}, message={}, availableCount={}",
                response.getOrderId(), response.getStatus(),
                response.getMessage(), response.getAvailableCount());

        Span span = tracer.spanBuilder("handleReservationResponse")
                .setAttribute("order.id", response.getOrderId())
                .setAttribute("response.status", response.getStatus().toString())
                .setAttribute("response.message", response.getMessage())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            UUID orderId = UUID.fromString(response.getOrderId());
            Reservation reservation = reservationRepository.findById(orderId)
                    .orElse(null);

            if (reservation == null) {
                log.warn("Reservation not found: {}", response.getOrderId());
                span.setAttribute("error", true);
                span.setAttribute("error.message", "Reservation not found");
                return;
            }

            if (response.getStatus() == ReservationStatus.SUCCESS) {
                reservation.setStatus(ReservationStatus.SUCCESS);
                log.info("Reservation {} confirmed successfully", orderId);
            } else {
                reservation.setStatus(ReservationStatus.FAIL);
                log.warn("Reservation {} failed: {}", orderId, response.getMessage());
            }

            reservationRepository.save(reservation);

            span.setAttribute("new.status", reservation.getStatus().toString());
            span.setAttribute("operation.success", true);

            log.info("Reservation {} status updated to: {}",
                    orderId, reservation.getStatus());

        } catch (Exception e) {
            log.error("Error processing reservation response", e);
            span.setAttribute("error", true);
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
        } finally {
            span.end();
        }
    }
}