package com.demo.be.service.impl;

import com.demo.be.dto.thongke.DashboardStatsResponse;
import com.demo.be.dto.thongke.ThongKeKhoaResponse;
import com.demo.be.dto.thongke.ThongKeXepLoaiResponse;
import com.demo.be.repository.DiemRenLuyenRepository;
import com.demo.be.repository.KhoaRepository;
import com.demo.be.repository.LopRepository;
import com.demo.be.repository.SinhVienRepository;
import com.demo.be.service.ThongKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ThongKeServiceImpl implements ThongKeService {

    private final SinhVienRepository sinhVienRepository;
    private final KhoaRepository khoaRepository;
    private final LopRepository lopRepository;
    private final DiemRenLuyenRepository diemRenLuyenRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long tongSinhVien = sinhVienRepository.count();
        long tongKhoa = khoaRepository.count();
        long tongLop = lopRepository.count();
        long choDuyet = diemRenLuyenRepository.countByTrangThai("CHO_DUYET");
        long daDuyet = diemRenLuyenRepository.countByTrangThai("DA_DUYET");

        // Sử dụng Native Query kết hợp @SqlResultSetMapping để tự động ánh xạ phân bổ xếp loại rèn luyện
        Map<String, Long> phanBo = new HashMap<>();
        phanBo.put("Xuất sắc", 0L);
        phanBo.put("Tốt", 0L);
        phanBo.put("Khá", 0L);
        phanBo.put("Trung bình", 0L);
        phanBo.put("Yếu", 0L);
        phanBo.put("Kém", 0L);

        List<ThongKeXepLoaiResponse> xepLoaiStats = diemRenLuyenRepository.thongKeXepLoaiMapping();
        for (ThongKeXepLoaiResponse item : xepLoaiStats) {
            if (item != null && item.xepLoai() != null && !item.xepLoai().isBlank()) {
                phanBo.put(item.xepLoai().trim(), item.soLuong());
            }
        }

        return new DashboardStatsResponse(
                tongSinhVien,
                tongKhoa,
                tongLop,
                choDuyet,
                daDuyet,
                phanBo
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThongKeKhoaResponse> getThongKeTheoKhoa() {
        // Sử dụng Native SQL kết hợp @SqlResultSetMapping để tự động ánh xạ trực tiếp sang DTO
        return diemRenLuyenRepository.thongKeDrlTheoKhoaMapping();
    }
}
