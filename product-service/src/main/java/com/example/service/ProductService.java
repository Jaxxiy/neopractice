package com.example.service;

import com.example.dto.ProductDTO;
import com.example.dto.ProductReservationMessage;
import com.example.dto.ReservationStatusMessage;
import com.example.entity.Product;
import com.example.enums.ReservationStatus;
import com.example.mapper.ProductMapper;
import com.example.repository.ProductRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing.response}")
    private String responseRoutingKey;

    public ProductService(ProductRepository productRepository,
                          ProductMapper productMapper,
                          RabbitTemplate rabbitTemplate) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products");
        return productMapper.toDtoList(productRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(UUID id) {
        log.info("Fetching product by id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDto) {
        log.info("Creating product: {}", productDto);

        Product product = productMapper.toEntity(productDto);
        product.setIdProduct(UUID.randomUUID());
        product.setDateTimeLastChange(LocalDateTime.now());

        Product saved = productRepository.save(product);
        log.info("Product created with id: {}", saved.getIdProduct());
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDTO updateProduct(UUID id, @Valid ProductDTO productDto) {
        log.info("Updating product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        productMapper.updateProductFromDto(productDto, product);
        product.setDateTimeLastChange(LocalDateTime.now());

        Product updated = productRepository.save(product);
        log.info("Product updated: {}", updated.getIdProduct());
        return productMapper.toDto(updated);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        log.info("Deleting product: {}", id);
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted: {}", id);
    }

    @Transactional
    public boolean reserveProduct(UUID productId, int quantity) {
        log.info("Reserving product: {}, quantity: {}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        if (product.getCount() < quantity) {
            log.warn("Insufficient stock. Available: {}, Requested: {}",
                    product.getCount(), quantity);
            return false;
        }

        product.setCount(product.getCount() - quantity);
        product.setDateTimeLastChange(LocalDateTime.now());
        productRepository.save(product);

        log.info("Product reserved successfully. New count: {}", product.getCount());
        return true;
    }

    @RabbitListener(queues = "${rabbitmq.queue.order}")
    @Transactional
    public void processReservation(ProductReservationMessage message) {
        log.info("Received reservation request: {}", message);

        String orderId = message.getOrderId();
        String productId = message.getProductId();
        int quantity = message.getCount();

        try {
            UUID productUUID = UUID.fromString(productId);
            Product product = productRepository.findById(productUUID)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            if (product.getCount() < quantity) {
                log.warn("Insufficient stock. Available: {}, Requested: {}",
                        product.getCount(), quantity);
                sendResponse(orderId, ReservationStatus.FAIL, "Insufficient stock");
                return;
            }

            product.setCount(product.getCount() - quantity);
            product.setDateTimeLastChange(LocalDateTime.now());
            productRepository.save(product);

            log.info("Product reserved. ID: {}, New count: {}", productId, product.getCount());
            sendResponse(orderId, ReservationStatus.SUCCESS, "Reservation successful");

        } catch (Exception e) {
            log.error("Error processing reservation", e);
            sendResponse(orderId, ReservationStatus.FAIL, "Error: " + e.getMessage());
        }
    }

    private void sendResponse(String orderId, ReservationStatus status, String message) {
        ReservationStatusMessage response = new ReservationStatusMessage(orderId, status, message);
        log.info("Sending response: {}", response);
        rabbitTemplate.convertAndSend(exchange, responseRoutingKey, response);
    }
}