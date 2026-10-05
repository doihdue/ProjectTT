package com.demo.be.repository;

import com.demo.be.model.Khoa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KhoaRepositoryCustom {
    Page<Khoa> searchAndFilter(String keyword, Pageable pageable);
}
