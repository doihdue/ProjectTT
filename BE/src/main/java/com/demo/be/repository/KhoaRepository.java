package com.demo.be.repository;

import com.demo.be.model.Khoa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KhoaRepository extends JpaRepository<Khoa, Long> {
    Optional<Khoa> findByMaKhoa(String maKhoa);
}
