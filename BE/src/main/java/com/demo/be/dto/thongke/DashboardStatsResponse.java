package com.demo.be.dto.thongke;

import java.util.Map;

public record DashboardStatsResponse(
        long tongSinhVien,
        long tongKhoa,
        long tongLop,
        long phieuDrlChoDuyet,
        long phieuDrlDaDuyet,
        Map<String, Long> phanBoXepLoaiDrl
) {}
