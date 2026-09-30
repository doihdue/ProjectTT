package com.demo.be.service;

import com.demo.be.dto.thongke.DashboardStatsResponse;
import com.demo.be.dto.thongke.ThongKeKhoaResponse;
import java.util.List;

public interface ThongKeService {

    DashboardStatsResponse getDashboardStats();

    List<ThongKeKhoaResponse> getThongKeTheoKhoa();
}
