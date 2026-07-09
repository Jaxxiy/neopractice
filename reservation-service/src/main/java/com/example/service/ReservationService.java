package com.example.service;

import com.example.dto.ProductReservationMessage;
import com.example.dto.ReservationDetailsDTO;
import com.example.dto.ReservationRequestDTO;
import com.example.dto.ReservationResponseDTO;
import com.example.entity.Reservation;
import com.example.enums.ReservationStatus;
import com.example.mapper.ReservationMapper;
import com.example.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange:order.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing.order:order.created}")
    private String orderRoutingKey;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationMapper reservationMapper,
                              RabbitTemplate rabbitTemplate) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO request) {
        log.info("Creating reservation for product: {}, count: {}, user: {}",
                request.getIdProduct(), request.getCount(), request.getIdUser());

        try {
            Reservation reservation = reservationMapper.toEntity(request);
            String orderId = UUID.randomUUID().toString();
            reservation.setIdReservation(UUID.fromString(orderId));
            reservation.setDateCreate(LocalDateTime.now());
            reservation.setStatus(ReservationStatus.PENDING);

            log.info("Step 1: Sending message to RabbitMQ...");
            ProductReservationMessage message = new ProductReservationMessage(
                    request.getIdProduct(),
                    request.getCount(),
                    orderId
            );

            rabbitTemplate.convertAndSend(
                    exchange,
                    orderRoutingKey,
                    message
            );
            log.info("Message sent to RabbitMQ successfully");

            log.info("Step 2: Saving reservation to database...");
            reservationRepository.save(reservation);
            log.info("Reservation saved with ID: {}", reservation.getIdReservation());


            reservation.setStatus(ReservationStatus.SUCCESS);
            reservationRepository.save(reservation);
            log.info("Reservation completed successfully: {}", reservation.getIdReservation());

            return reservationMapper.createResponseDto(
                    "success",
                    reservation.getIdReservation().toString(),
                    "Reservation created and message sent to queue"
            );

        } catch (Exception e) {
            log.error("Error creating reservation", e);

            return reservationMapper.createResponseDto(
                    "fail",
                    null,
                    "Failed to create reservation: " + e.getMessage()
            );
        }
    }


    private boolean reserveProduct(String productId, int quantity) {
        try {
            log.info("Calling Product Service to reserve product: {}, quantity: {}", productId, quantity);
            return true;
        } catch (Exception e) {
            log.error("Error calling Product Service", e);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationDetailsDTO> getAllReservations() {
        log.info("Fetching all reservations");
        return reservationMapper.toDetailsDtoList(reservationRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ReservationDetailsDTO getReservationById(UUID id) {
        log.info("Fetching reservation by id: {}", id);
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));
        return reservationMapper.toDetailsDto(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationDetailsDTO> getReservationsByUser(String userId) {
        log.info("Fetching reservations for user: {}", userId);
        return reservationMapper.toDetailsDtoList(reservationRepository.findByIdUser(userId));
    }

    @Transactional(readOnly = true)
    public List<ReservationDetailsDTO> getReservationsByProduct(String productId) {
        log.info("Fetching reservations for product: {}", productId);
        return reservationMapper.toDetailsDtoList(reservationRepository.findByIdProduct(productId));
    }

    @Transactional
    public void cancelReservation(UUID id) {
        log.info("Cancelling reservation: {}", id);
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));

        reservation.setStatus(ReservationStatus.FAIL);
        reservationRepository.save(reservation);
        log.info("Reservation cancelled: {}", id);
    }
}
