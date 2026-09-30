package com.demo.be.dto.diemrenluyen;

import java.util.Map;

public record DrlThongKeResponse(
        long tongSoPhieu,
        long soChoDuyet,
        long soDaDuyet,
        long soTuChoi,
        Map<String, Long> phanBoXepLoai
) {}
