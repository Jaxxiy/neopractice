package com.example.service;

import com.example.dto.ProductDTO;
import com.example.dto.ProductReservationMessage;
import com.example.dto.ReservationStatusMessage;
import com.example.entity.Product;
import com.example.enums.ReservationStatus;
import com.example.mapper.ProductMapper;
import com.example.repository.ProductRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
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
    private final Tracer tracer;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing.response}")
    private String responseRoutingKey;

    public ProductService(ProductRepository productRepository,
                          ProductMapper productMapper,
                          RabbitTemplate rabbitTemplate,
                          Tracer tracer) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.tracer = tracer;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        Span span = tracer.spanBuilder("getAllProducts")
                .setAttribute("operation.type", "read_all")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            log.info("Fetching all products");

            long startTime = System.currentTimeMillis();
            List<ProductDTO> result = productMapper.toDtoList(productRepository.findAll());
            long executionTime = System.currentTimeMillis() - startTime;

            span.setAttribute("result.count", result.size());
            span.setAttribute("operation.success", true);
            span.setAttribute("execution.time.ms", executionTime);

            log.info("Fetched {} products in {}ms", result.size(), executionTime);
            return result;
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.type", e.getClass().getSimpleName());
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @Transactional(readOnly = true)
    public ProductDTO getProductById(UUID id) {
        Span span = tracer.spanBuilder("getProductById")
                .setAttribute("product.id", id.toString())
                .setAttribute("operation.type", "read_by_id")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            log.info("Fetching product by id: {}", id);

            long startTime = System.currentTimeMillis();
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
            long executionTime = System.currentTimeMillis() - startTime;

            span.setAttribute("operation.success", true);
            span.setAttribute("execution.time.ms", executionTime);
            span.setAttribute("product.found", true);

            log.info("Product fetched in {}ms", executionTime);
            return productMapper.toDto(product);
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.type", e.getClass().getSimpleName());
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDto) {
        Span span = tracer.spanBuilder("createProduct")
                .setAttribute("product.name", productDto.getName())
                .setAttribute("product.count", productDto.getCount())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            log.info("Creating product: {}", productDto);

            Product product = productMapper.toEntity(productDto);
            product.setIdProduct(UUID.randomUUID());
            product.setDateTimeLastChange(LocalDateTime.now());

            long startTime = System.currentTimeMillis();
            Product saved = productRepository.save(product);
            long executionTime = System.currentTimeMillis() - startTime;

            span.setAttribute("product.id", saved.getIdProduct().toString());
            span.setAttribute("operation.success", true);
            span.setAttribute("execution.time.ms", executionTime);
            span.setAttribute("db.rows_affected", 1);

            log.info("Product created with id: {} in {}ms", saved.getIdProduct(), executionTime);
            return productMapper.toDto(saved);
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.type", e.getClass().getSimpleName());
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @Transactional
    public ProductDTO updateProduct(UUID id, @Valid ProductDTO productDto) {
        Span span = tracer.spanBuilder("updateProduct")
                .setAttribute("product.id", id.toString())
                .setAttribute("product.name", productDto.getName())
                .setAttribute("product.count", productDto.getCount())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            log.info("Updating product: {}", id);

            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

            productMapper.updateProductFromDto(productDto, product);
            product.setDateTimeLastChange(LocalDateTime.now());

            long startTime = System.currentTimeMillis();
            Product updated = productRepository.save(product);
            long executionTime = System.currentTimeMillis() - startTime;

            span.setAttribute("operation.success", true);
            span.setAttribute("execution.time.ms", executionTime);
            span.setAttribute("db.rows_affected", 1);

            log.info("Product updated: {} in {}ms", updated.getIdProduct(), executionTime);
            return productMapper.toDto(updated);
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.type", e.getClass().getSimpleName());
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Span span = tracer.spanBuilder("deleteProduct")
                .setAttribute("product.id", id.toString())
                .setAttribute("operation.type", "delete")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            log.info("Deleting product: {}", id);

            if (!productRepository.existsById(id)) {
                throw new RuntimeException("Product not found with id: " + id);
            }

            long startTime = System.currentTimeMillis();
            productRepository.deleteById(id);
            long executionTime = System.currentTimeMillis() - startTime;

            span.setAttribute("operation.success", true);
            span.setAttribute("execution.time.ms", executionTime);
            span.setAttribute("db.rows_affected", 1);

            log.info("Product deleted: {} in {}ms", id, executionTime);
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.type", e.getClass().getSimpleName());
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @Transactional
    public boolean reserveProduct(UUID productId, int quantity) {
        Span span = tracer.spanBuilder("reserveProduct")
                .setAttribute("product.id", productId.toString())
                .setAttribute("quantity", quantity)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            log.info("Reserving product: {}, quantity: {}", productId, quantity);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

            if (product.getCount() < quantity) {
                log.warn("Insufficient stock. Available: {}, Requested: {}",
                        product.getCount(), quantity);
                span.setAttribute("success", false);
                span.setAttribute("reason", "Insufficient stock");
                span.setAttribute("available", product.getCount());
                return false;
            }

            int oldCount = product.getCount();
            product.setCount(product.getCount() - quantity);
            product.setDateTimeLastChange(LocalDateTime.now());

            long startTime = System.currentTimeMillis();
            productRepository.save(product);
            long executionTime = System.currentTimeMillis() - startTime;

            span.setAttribute("success", true);
            span.setAttribute("old.count", oldCount);
            span.setAttribute("new.count", product.getCount());
            span.setAttribute("reserved.quantity", quantity);
            span.setAttribute("execution.time.ms", executionTime);

            log.info("Product reserved successfully. New count: {} in {}ms",
                    product.getCount(), executionTime);
            return true;
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.type", e.getClass().getSimpleName());
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.order}")
    @Transactional
    public void processReservation(ProductReservationMessage message) {
        log.info("=== RECEIVED RESERVATION REQUEST ===");
        log.info("Message: {}", message);

        Span mainSpan = tracer.spanBuilder("processReservation")
                .setAttribute("product.id", message.getProductId())
                .setAttribute("product.count", message.getCount())
                .setAttribute("order.id", message.getOrderId())
                .startSpan();

        try (Scope scope = mainSpan.makeCurrent()) {
            String orderId = message.getOrderId();
            String productId = message.getProductId();
            int quantity = message.getCount();

            Product product = findProductById(productId);
            if (product == null) {
                log.warn("Product not found: {}", productId);
                sendResponse(orderId, ReservationStatus.FAIL, "Product not found: " + productId);
                mainSpan.setAttribute("status", "FAIL");
                mainSpan.setAttribute("reason", "Product not found");
                return;
            }

            if (!checkStock(product, quantity)) {
                log.warn("Insufficient stock. Available: {}, Requested: {}",
                        product.getCount(), quantity);
                sendResponse(orderId, ReservationStatus.FAIL,
                        "Insufficient stock. Available: " + product.getCount());
                mainSpan.setAttribute("status", "FAIL");
                mainSpan.setAttribute("reason", "Insufficient stock");
                mainSpan.setAttribute("available", product.getCount());
                return;
            }

            updateProductCount(product, quantity);

            log.info("Product reserved. ID: {}, New count: {}", productId, product.getCount());
            sendResponse(orderId, ReservationStatus.SUCCESS, "Reservation successful");

            mainSpan.setAttribute("status", "SUCCESS");
            mainSpan.setAttribute("new.count", product.getCount());

        } catch (Exception e) {
            mainSpan.setAttribute("error", true);
            mainSpan.setAttribute("error.type", e.getClass().getSimpleName());
            mainSpan.setAttribute("error.message", e.getMessage());
            mainSpan.recordException(e);
            log.error("Error processing reservation", e);
            sendResponse(message.getOrderId(), ReservationStatus.FAIL, "Error: " + e.getMessage());
        } finally {
            mainSpan.end();
        }
    }

    private Product findProductById(String productId) {
        Span span = tracer.spanBuilder("findProductById")
                .setAttribute("product.id", productId)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            long startTime = System.currentTimeMillis();
            Product product = productRepository.findById(UUID.fromString(productId))
                    .orElse(null);
            long executionTime = System.currentTimeMillis() - startTime;

            span.setAttribute("product.found", product != null);
            span.setAttribute("execution.time.ms", executionTime);

            log.debug("Product {} found: {} in {}ms", productId, product != null, executionTime);
            return product;
        } finally {
            span.end();
        }
    }

    private boolean checkStock(Product product, int requestedCount) {
        Span span = tracer.spanBuilder("checkStock")
                .setAttribute("product.id", product.getIdProduct().toString())
                .setAttribute("available", product.getCount())
                .setAttribute("requested", requestedCount)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            boolean hasStock = product.getCount() >= requestedCount;
            span.setAttribute("has.stock", hasStock);
            return hasStock;
        } finally {
            span.end();
        }
    }

    private void updateProductCount(Product product, int quantity) {
        Span span = tracer.spanBuilder("updateProductCount")
                .setAttribute("product.id", product.getIdProduct().toString())
                .setAttribute("old.count", product.getCount())
                .setAttribute("subtract", quantity)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            long startTime = System.currentTimeMillis();

            product.setCount(product.getCount() - quantity);
            product.setDateTimeLastChange(LocalDateTime.now());
            productRepository.save(product);

            long executionTime = System.currentTimeMillis() - startTime;

            span.setAttribute("new.count", product.getCount());
            span.setAttribute("operation.success", true);
            span.setAttribute("execution.time.ms", executionTime);

            log.debug("Product count updated in {}ms", executionTime);
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.type", e.getClass().getSimpleName());
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private void sendResponse(String orderId, ReservationStatus status, String message) {
        Span span = tracer.spanBuilder("sendResponse")
                .setAttribute("order.id", orderId)
                .setAttribute("status", status.toString())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            ReservationStatusMessage response = new ReservationStatusMessage(orderId, status, message);
            log.info("Sending response: {}", response);
            rabbitTemplate.convertAndSend(exchange, responseRoutingKey, response);
            span.setAttribute("message.sent", true);
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