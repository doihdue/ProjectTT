package com.demo.be.dto.sinhvien;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StudentProfileUpdateRequest(
        @NotBlank(message = "Email is required")
        @Pattern(regexp = "^[^\\s@]+@[^\\s@]+$", message = "Email is invalid")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email,
        @Size(max = 20, message = "So dien thoai must be at most 20 characters")
        String soDienThoai,
        @Size(max = 255, message = "Dia chi must be at most 255 characters")
        String diaChi
) {
}