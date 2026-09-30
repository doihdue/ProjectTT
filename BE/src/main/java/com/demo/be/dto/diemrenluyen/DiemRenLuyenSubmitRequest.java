package com.demo.be.dto.diemrenluyen;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DiemRenLuyenSubmitRequest(
        Long dotDanhGiaId,
        Integer hocKy,
        String namHoc,

        @NotNull(message = "Điểm tiêu chí 1 không được trống")
        @Min(value = 0, message = "Tiêu chí 1 tối thiểu là 0")
        @Max(value = 20, message = "Tiêu chí 1 tối đa là 20")
        Integer tieuChi1Sv,

        @NotNull(message = "Điểm tiêu chí 2 không được trống")
        @Min(value = 0, message = "Tiêu chí 2 tối thiểu là 0")
        @Max(value = 25, message = "Tiêu chí 2 tối đa là 25")
        Integer tieuChi2Sv,

        @NotNull(message = "Điểm tiêu chí 3 không được trống")
        @Min(value = 0, message = "Tiêu chí 3 tối thiểu là 0")
        @Max(value = 20, message = "Tiêu chí 3 tối đa là 20")
        Integer tieuChi3Sv,

        @NotNull(message = "Điểm tiêu chí 4 không được trống")
        @Min(value = 0, message = "Tiêu chí 4 tối thiểu là 0")
        @Max(value = 25, message = "Tiêu chí 4 tối đa là 25")
        Integer tieuChi4Sv,

        @NotNull(message = "Điểm tiêu chí 5 không được trống")
        @Min(value = 0, message = "Tiêu chí 5 tối thiểu là 0")
        @Max(value = 10, message = "Tiêu chí 5 tối đa là 10")
        Integer tieuChi5Sv,

        String ghiChuSinhVien,
        String minhChung
) {}
