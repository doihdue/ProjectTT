package com.demo.be.controller;

import com.demo.be.dto.khoa.KhoaRequest;
import com.demo.be.dto.khoa.KhoaResponse;
import com.demo.be.service.KhoaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/khoa")
public class KhoaController {

    private final KhoaService khoaService;

    public KhoaController(KhoaService khoaService) {
        this.khoaService = khoaService;
    }

    @GetMapping
    public ResponseEntity<List<KhoaResponse>> getAll() {
        return ResponseEntity.ok(khoaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<KhoaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(khoaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<KhoaResponse> create(@Valid @RequestBody KhoaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(khoaService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<KhoaResponse> update(@PathVariable Long id, @Valid @RequestBody KhoaRequest request) {
        return ResponseEntity.ok(khoaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        khoaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
