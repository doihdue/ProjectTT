package com.demo.be.controller;

import com.demo.be.dto.thongke.DashboardStatsResponse;
import com.demo.be.service.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/thong-ke")
@RequiredArgsConstructor
public class ThongKeController {

    private final ThongKeService thongKeService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(thongKeService.getDashboardStats());
    }

    /**
     * API Thống kê điểm rèn luyện tổng hợp theo từng Khoa
     * Áp dụng đúng kiến trúc: Controller -> Service -> Repository (sử dụng NativeQuery)
     */
    @GetMapping("/khoa")
    public ResponseEntity<java.util.List<com.demo.be.dto.thongke.ThongKeKhoaResponse>> getThongKeTheoKhoa() {
        return ResponseEntity.ok(thongKeService.getThongKeTheoKhoa());
    }
}

