package com.demo.be.dto.khoa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KhoaRequest(
        @NotBlank(message = "Ma khoa is required") @Size(max = 20, message = "Ma khoa must be at most 20 characters") String maKhoa,
        @NotBlank(message = "Ten khoa is required") @Size(max = 150, message = "Ten khoa must be at most 150 characters") String tenKhoa,
        @Size(max = 500, message = "Mo ta must be at most 500 characters") String moTa,
        Boolean active
) {
}
