package com.demo.be.dto.lop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LopRequest(
        @Size(max = 20, message = "Ma lop must be at most 20 characters") String maLop,
        @NotBlank(message = "Ten lop is required") @Size(max = 150, message = "Ten lop must be at most 150 characters") String tenLop,
        @NotBlank(message = "Nien khoa is required") @Size(max = 20, message = "Nien khoa must be at most 20 characters") String nienKhoa,
        @NotNull(message = "Si so toi da is required") @Positive(message = "Si so toi da must be positive") Integer siSoToiDa,
        @NotNull(message = "Khoa id is required") Long khoaId,
        Boolean active
) {
}
