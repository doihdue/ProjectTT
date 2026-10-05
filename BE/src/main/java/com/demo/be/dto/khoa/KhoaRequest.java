package com.demo.be.dto.khoa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KhoaRequest(
        @NotBlank(message = "Mã khoa không được để trống") @Size(max = 20, message = "Mã khoa tối đa 20 ký tự") String maKhoa,
        @NotBlank(message = "Tên khoa không được để trống") @Size(max = 150, message = "Tên khoa tối đa 150 ký tự") String tenKhoa,
        @Size(max = 500, message = "Mô tả tối đa 500 ký tự") String moTa,
        Boolean active
) {
}
