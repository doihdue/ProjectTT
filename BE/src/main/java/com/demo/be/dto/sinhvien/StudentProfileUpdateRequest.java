package com.demo.be.dto.sinhvien;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StudentProfileUpdateRequest(
        @NotBlank(message = "Email không được để trống")
        @Pattern(regexp = "^[^\\s@]+@[^\\s@]+$", message = "Email không đúng định dạng")
        @Size(max = 150, message = "Email tối đa 150 ký tự")
        String email,
        @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
        String soDienThoai,
        @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
        String diaChi
) {
}