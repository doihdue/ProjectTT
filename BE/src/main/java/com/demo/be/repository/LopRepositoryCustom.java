package com.demo.be.repository;

import com.demo.be.model.Lop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LopRepositoryCustom {
    Page<Lop> searchAndFilter(String keyword, Long khoaId, Pageable pageable);
}
