package com.shop.shop.controller;

import com.shop.shop.model.Product;
import com.shop.shop.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository repo;

    @PostMapping
    public ResponseEntity<Product> addProduct(@Valid @RequestBody Product product) {
        Product saved = repo.save(product);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping
    public List<Product> getProducts(@RequestParam(required = false) String category,
                                     @RequestParam(required = false) String name) {
        if (category != null) {
            return repo.findByCategoryIgnoreCase(category);
        }
        if (name != null) {
            return repo.findByNameContainingIgnoreCase(name);
        }
        return repo.findAll();
    }

    @GetMapping("/count")
    public long countProducts() {
        return repo.count();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getProduct(@PathVariable Long id) {
        Optional<Product> found = repo.findById(id);

        if (found.isEmpty()) {
            return ResponseEntity.status(404).body("Product not found");
        }
        return ResponseEntity.ok(found.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable Long id,
                                                @Valid @RequestBody Product newData) {
        Optional<Product> found = repo.findById(id);

        if (found.isEmpty()) {
            return ResponseEntity.status(404).body("Product not found");
        }

        Product product = found.get();
        product.setName(newData.getName());
        product.setCategory(newData.getCategory());
        product.setPrice(newData.getPrice());
        product.setStock(newData.getStock());

        return ResponseEntity.ok(repo.save(product));
    }

    @PatchMapping("/{id}/buy")
    public ResponseEntity<Object> buyProduct(@PathVariable Long id,
                                             @RequestParam int quantity) {
        Optional<Product> found = repo.findById(id);

        if (found.isEmpty()) {
            return ResponseEntity.status(404).body("Product not found");
        }

        Product product = found.get();

        if (quantity <= 0) {
            return ResponseEntity.badRequest().body("Quantity must be at least 1");
        }
        if (quantity > product.getStock()) {
            return ResponseEntity.badRequest().body("Not enough stock. Only " + product.getStock() + " left");
        }

        product.setStock(product.getStock() - quantity);
        return ResponseEntity.ok(repo.save(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.status(404).body("Product not found");
        }

        repo.deleteById(id);
        return ResponseEntity.ok("Product deleted");
    }
}