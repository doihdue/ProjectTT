package com.demo.be.controller;

import com.demo.be.dto.common.PageResponse;
import com.demo.be.dto.sinhvien.SinhVienRequest;
import com.demo.be.dto.sinhvien.SinhVienResponse;
import com.demo.be.dto.sinhvien.StudentProfileUpdateRequest;
import com.demo.be.service.SinhVienService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api/sinh-vien")
public class SinhVienController {

    private final SinhVienService sinhVienService;

    public SinhVienController(SinhVienService sinhVienService) {
        this.sinhVienService = sinhVienService;
    }

    @GetMapping
    public ResponseEntity<List<SinhVienResponse>> getAll() {
        return ResponseEntity.ok(sinhVienService.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<SinhVienResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long lopId,
            @RequestParam(required = false) String gioiTinh,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(sinhVienService.searchAndFilter(keyword, lopId, gioiTinh, page, size));
    }

    @GetMapping("/next-mssv")
    public ResponseEntity<Map<String, String>> getNextMssv(
            @RequestParam Long lopId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayNhapHoc,
            @RequestParam(required = false) Integer year
    ) {
        LocalDate date = ngayNhapHoc;
        if (date == null && year != null) {
            date = LocalDate.of(year, 1, 1);
        }
        String nextMssv = sinhVienService.generateNextMssv(lopId, date);
        return ResponseEntity.ok(Map.of("mssv", nextMssv));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SinhVienResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sinhVienService.findById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<SinhVienResponse> getCurrent(Principal principal) {
        return ResponseEntity.ok(sinhVienService.findByMssv(principal.getName()));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<SinhVienResponse> updateCurrentProfile(
            Principal principal,
            @Valid @RequestBody StudentProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(sinhVienService.updateProfile(principal.getName(), request));
    }

    @PostMapping
    public ResponseEntity<SinhVienResponse> create(@Valid @RequestBody SinhVienRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sinhVienService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SinhVienResponse> update(@PathVariable Long id, @Valid @RequestBody SinhVienRequest request) {
        return ResponseEntity.ok(sinhVienService.update(id, request));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<String> resetPassword(@PathVariable Long id) {
        return ResponseEntity.ok(sinhVienService.resetPassword(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sinhVienService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
