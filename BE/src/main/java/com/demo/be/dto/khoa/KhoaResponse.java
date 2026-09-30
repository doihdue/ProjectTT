package com.demo.be.dto.khoa;

public record KhoaResponse(
        Long id,
        String maKhoa,
        String tenKhoa,
        String moTa,
        boolean active
) {
}
