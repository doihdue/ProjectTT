package com.demo.be.controller;

import com.demo.be.dto.common.PageResponse;
import com.demo.be.dto.lop.LopRequest;
import com.demo.be.dto.lop.LopResponse;
import com.demo.be.service.LopService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lop")
public class LopController {

    private final LopService lopService;

    public LopController(LopService lopService) {
        this.lopService = lopService;
    }

    @GetMapping
    public ResponseEntity<List<LopResponse>> getAll() {
        return ResponseEntity.ok(lopService.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<LopResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long khoaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(lopService.searchAndFilter(keyword, khoaId, page, size));
    }

    @GetMapping("/next-ma-lop")
    public ResponseEntity<Map<String, String>> getNextMaLop(
            @RequestParam Long khoaId,
            @RequestParam(required = false) String nienKhoa
    ) {
        String nextMaLop = lopService.generateNextMaLop(khoaId, nienKhoa);
        return ResponseEntity.ok(Map.of("maLop", nextMaLop));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LopResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(lopService.findById(id));
    }

    @PostMapping
    public ResponseEntity<LopResponse> create(@Valid @RequestBody LopRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lopService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LopResponse> update(@PathVariable Long id, @Valid @RequestBody LopRequest request) {
        return ResponseEntity.ok(lopService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lopService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
