package com.demo.be.repository;

import com.demo.be.dto.thongke.ThongKeKhoaResponse;
import com.demo.be.dto.thongke.ThongKeXepLoaiResponse;
import java.util.List;

public interface DiemRenLuyenRepositoryCustom {

    /**
     * Thống kê điểm rèn luyện theo từng khoa sử dụng EntityManager kết hợp @SqlResultSetMapping
     */
    List<ThongKeKhoaResponse> thongKeDrlTheoKhoaMapping();

    /**
     * Thống kê xếp loại rèn luyện sử dụng EntityManager kết hợp @SqlResultSetMapping
     */
    List<ThongKeXepLoaiResponse> thongKeXepLoaiMapping();
}
