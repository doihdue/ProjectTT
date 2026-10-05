package com.demo.be.dto.sinhvien;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SinhVienRequest(
        @Size(max = 20, message = "MSSV tối đa 20 ký tự") String mssv,
        @NotBlank(message = "Họ tên sinh viên không được để trống") @Size(max = 150, message = "Họ tên tối đa 150 ký tự") String hoTen,
        @NotNull(message = "Ngày sinh không được để trống")
        @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
        LocalDate ngaySinh,
        @Size(max = 10, message = "Giới tính tối đa 10 ký tự") String gioiTinh,
        @Size(max = 150, message = "Email tối đa 150 ký tự") String email,
        @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự") String soDienThoai,
        @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự") String diaChi,
        LocalDate ngayNhapHoc,
        @NotNull(message = "Vui lòng chọn Lớp học") Long lopId,
        Boolean active
) {
}
