package com.demo.be.dto.lop;

public record LopResponse(
        Long id,
        String maLop,
        String tenLop,
        String nienKhoa,
        Integer siSoToiDa,
        Long khoaId,
        String khoaTen,
        String maKhoa,
        boolean active
) {
}
