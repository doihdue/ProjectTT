package com.demo.be.repository;

import com.demo.be.model.SinhVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SinhVienRepositoryCustom {
    Page<SinhVien> searchAndFilter(String keyword, Long lopId, String gioiTinh, Pageable pageable);
}
