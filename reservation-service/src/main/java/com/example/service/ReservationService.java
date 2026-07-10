package com.example.service;

import com.example.dto.ProductReservationMessage;
import com.example.dto.ReservationDetailsDTO;
import com.example.dto.ReservationRequestDTO;
import com.example.dto.ReservationResponseDTO;
import com.example.entity.Reservation;
import com.example.enums.ReservationStatus;
import com.example.mapper.ReservationMapper;
import com.example.repository.ReservationRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final RabbitTemplate rabbitTemplate;
    private final Tracer tracer;

    @Value("${rabbitmq.exchange:order.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing.order:order.created}")
    private String orderRoutingKey;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationMapper reservationMapper,
                              RabbitTemplate rabbitTemplate,
                              Tracer tracer) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.tracer = tracer;
    }

    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO request) {
        log.info("Creating reservation for product: {}, count: {}, user: {}",
                request.getIdProduct(), request.getCount(), request.getIdUser());

        Span mainSpan = tracer.spanBuilder("createReservation")
                .setAttribute("product.id", request.getIdProduct())
                .setAttribute("product.count", request.getCount())
                .setAttribute("user.id", request.getIdUser())
                .startSpan();

        try (Scope scope = mainSpan.makeCurrent()) {
            Reservation reservation = reservationMapper.toEntity(request);
            String orderId = UUID.randomUUID().toString();
            reservation.setIdReservation(UUID.fromString(orderId));
            reservation.setDateCreate(LocalDateTime.now());
            reservation.setStatus(ReservationStatus.PENDING);

            reservationRepository.save(reservation);
            log.info("Reservation saved with ID:{} PENDING",orderId);

            sendToRabbitMQ(request, orderId);
            log.info("Reservation created, waiting Product Service");

            return reservationMapper.createResponseDto(
                    "pending",
                    orderId,
                    "Reservation created, waiting"
            );

        } catch (Exception e) {
            log.error("Error creating reservation", e);
            mainSpan.setAttribute("error", true);
            mainSpan.setAttribute("error.message", e.getMessage());
            mainSpan.recordException(e);

            return reservationMapper.createResponseDto(
                    "fail",
                    null,
                    "Failed to create reservation: " + e.getMessage()
            );
        } finally {
            mainSpan.end();
        }
    }

    private void sendToRabbitMQ(ReservationRequestDTO request, String orderId) {
        Span rabbitSpan = tracer.spanBuilder("sendToRabbitMQ")
                .setAttribute("exchange", exchange)
                .setAttribute("routing.key", orderRoutingKey)
                .setAttribute("product.id", request.getIdProduct())
                .setAttribute("order.id", orderId)
                .startSpan();

        try (Scope scope = rabbitSpan.makeCurrent()) {
            log.info("Sending message to RabbitMQ...");

            ProductReservationMessage message = new ProductReservationMessage(
                    request.getIdProduct(),
                    request.getCount(),
                    orderId,
                    request.getIdUser()
            );

            rabbitTemplate.convertAndSend(
                    exchange,
                    orderRoutingKey,
                    message
            );

            rabbitSpan.setAttribute("message.sent", true);
            log.info("Message sent to RabbitMQ successfully");
        } catch (Exception e) {
            rabbitSpan.setAttribute("error", true);
            rabbitSpan.setAttribute("error.message", e.getMessage());
            rabbitSpan.recordException(e);
            throw e;
        } finally {
            rabbitSpan.end();
        }
    }
    /*
    private void saveReservationToDatabase(Reservation reservation) {
        Span dbSpan = tracer.spanBuilder("saveReservationToDatabase")
                .setAttribute("reservation.id", reservation.getIdReservation().toString())
                .setAttribute("entity", "Reservation")
                .startSpan();

        try (Scope scope = dbSpan.makeCurrent()) {
            log.info("Step 2: Saving reservation to database...");
            reservationRepository.save(reservation);
            dbSpan.setAttribute("db.insert.success", true);
            log.info("Reservation saved with ID: {}", reservation.getIdReservation());
        } catch (Exception e) {
            dbSpan.setAttribute("error", true);
            dbSpan.setAttribute("error.message", e.getMessage());
            dbSpan.recordException(e);
            throw e;
        } finally {
            dbSpan.end();
        }
    }

    private boolean reserveProduct(String productId, int quantity) {
        Span span = tracer.spanBuilder("reserveProduct")
                .setAttribute("product.id", productId)
                .setAttribute("product.quantity", quantity)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            log.info("Calling Product Service to reserve product: {}, quantity: {}", productId, quantity);
            span.setAttribute("operation.success", true);
            return true;
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            log.error("Error calling Product Service", e);
            return false;
        } finally {
            span.end();
        }
    }

     */

    @Transactional(readOnly = true)
    public List<ReservationDetailsDTO> getAllReservations() {
        Span span = tracer.spanBuilder("getAllReservations")
                .setAttribute("operation.type", "read_all")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            log.info("Fetching all reservations");
            List<ReservationDetailsDTO> result = reservationMapper.toDetailsDtoList(reservationRepository.findAll());
            span.setAttribute("result.count", result.size());
            span.setAttribute("operation.success", true);
            return result;
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @Transactional(readOnly = true)
    public ReservationDetailsDTO getReservationById(UUID id) {
        Span span = tracer.spanBuilder("getReservationById")
                .setAttribute("reservation.id", id.toString())
                .setAttribute("operation.type", "read_by_id")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            log.info("Fetching reservation by id: {}", id);
            Reservation reservation = reservationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));

            span.setAttribute("operation.success", true);
            return reservationMapper.toDetailsDto(reservation);
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationDetailsDTO> getReservationsByUser(String userId) {
        Span span = tracer.spanBuilder("getReservationsByUser")
                .setAttribute("user.id", userId)
                .setAttribute("operation.type", "read_by_user")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            log.info("Fetching reservations for user: {}", userId);
            List<ReservationDetailsDTO> result = reservationMapper.toDetailsDtoList(
                    reservationRepository.findByIdUser(userId));

            span.setAttribute("result.count", result.size());
            span.setAttribute("operation.success", true);
            return result;
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationDetailsDTO> getReservationsByProduct(String productId) {
        Span span = tracer.spanBuilder("getReservationsByProduct")
                .setAttribute("product.id", productId)
                .setAttribute("operation.type", "read_by_product")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            log.info("Fetching reservations for product: {}", productId);
            List<ReservationDetailsDTO> result = reservationMapper.toDetailsDtoList(
                    reservationRepository.findByIdProduct(productId));

            span.setAttribute("result.count", result.size());
            span.setAttribute("operation.success", true);
            return result;
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @Transactional
    public void cancelReservation(UUID id) {
        Span span = tracer.spanBuilder("cancelReservation")
                .setAttribute("reservation.id", id.toString())
                .setAttribute("operation.type", "cancel")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            log.info("Cancelling reservation: {}", id);
            Reservation reservation = reservationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));

            reservation.setStatus(ReservationStatus.FAIL);
            reservationRepository.save(reservation);

            span.setAttribute("operation.success", true);
            span.setAttribute("new.status", "FAIL");
            log.info("Reservation cancelled: {}", id);
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}