package com.example.controller;

import com.example.dto.ProductDTO;
import com.example.service.ProductService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    private final Tracer tracer;

    public ProductController(ProductService productService, Tracer tracer) {
        this.productService = productService;
        this.tracer = tracer;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        log.info("Received request to get all products");

        Span span = tracer.spanBuilder("GET /api/products")
                .setAttribute("operation.type", "get_all")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            List<ProductDTO> products = productService.getAllProducts();
            span.setAttribute("result.count", products.size());
            span.setAttribute("operation.success", true);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable UUID id) {
        log.info("Received request to get product by id: {}", id);

        Span span = tracer.spanBuilder("GET /api/products/{id}")
                .setAttribute("product.id", id.toString())
                .setAttribute("operation.type", "get_by_id")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            ProductDTO product = productService.getProductById(id);
            span.setAttribute("operation.success", true);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDto) {
        log.info("Received request to create product: {}", productDto);

        Span span = tracer.spanBuilder("POST /api/products")
                .setAttribute("product.name", productDto.getName())
                .setAttribute("product.count", productDto.getCount())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            ProductDTO created = productService.createProduct(productDto);
            span.setAttribute("product.id", created.getIdProduct().toString());
            span.setAttribute("operation.success", true);
            return ResponseEntity.created(URI.create("/api/products/" + created.getIdProduct()))
                    .body(created);
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductDTO productDto) {
        log.info("Received request to update product: {}", id);

        Span span = tracer.spanBuilder("PUT /api/products/{id}")
                .setAttribute("product.id", id.toString())
                .setAttribute("product.name", productDto.getName())
                .setAttribute("product.count", productDto.getCount())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            ProductDTO updated = productService.updateProduct(id, productDto);
            span.setAttribute("operation.success", true);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        log.info("Received request to delete product: {}", id);

        Span span = tracer.spanBuilder("DELETE /api/products/{id}")
                .setAttribute("product.id", id.toString())
                .setAttribute("operation.type", "delete")
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            productService.deleteProduct(id);
            span.setAttribute("operation.success", true);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.message", e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @PostMapping("/{id}/reserve")
    public ResponseEntity<Boolean> reserveProduct(
            @PathVariable UUID id,
            @RequestParam int quantity) {
        log.info("Received request to reserve product: {}, quantity: {}", id, quantity);

        Span span = tracer.spanBuilder("POST /api/products/{id}/reserve")
                .setAttribute("product.id", id.toString())
                .setAttribute("quantity", quantity)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            boolean result = productService.reserveProduct(id, quantity);
            span.setAttribute("reservation.result", result);
            span.setAttribute("operation.success", true);
            return ResponseEntity.ok(result);
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