package com.demo.be.controller;

import com.demo.be.dto.diemrenluyen.DotDanhGiaRequest;
import com.demo.be.dto.diemrenluyen.DotDanhGiaResponse;
import com.demo.be.service.DotDanhGiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/api/dot-danh-gia")
@RequiredArgsConstructor
public class DotDanhGiaController {

    private final DotDanhGiaService dotDanhGiaService;

    @GetMapping
    public ResponseEntity<List<DotDanhGiaResponse>> getAllDots() {
        return ResponseEntity.ok(dotDanhGiaService.getAllDots());
    }

    @GetMapping("/open")
    public ResponseEntity<List<DotDanhGiaResponse>> getOpenDots() {
        return ResponseEntity.ok(dotDanhGiaService.getOpenDots());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DotDanhGiaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(dotDanhGiaService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DotDanhGiaResponse> createDot(@Valid @RequestBody DotDanhGiaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dotDanhGiaService.createDot(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DotDanhGiaResponse> updateDot(
            @PathVariable Long id,
            @Valid @RequestBody DotDanhGiaRequest request
    ) {
        return ResponseEntity.ok(dotDanhGiaService.updateDot(id, request));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<DotDanhGiaResponse> toggleTrangThai(@PathVariable Long id) {
        return ResponseEntity.ok(dotDanhGiaService.toggleTrangThai(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDot(@PathVariable Long id) {
        dotDanhGiaService.deleteDot(id);
        return ResponseEntity.noContent().build();
    }
}
