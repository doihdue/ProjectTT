package com.demo.be.repository;

import com.demo.be.model.DotDanhGiaDrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DotDanhGiaRepository extends JpaRepository<DotDanhGiaDrl, Long> {

    List<DotDanhGiaDrl> findAllByOrderByThoiGianTaoDesc();

    List<DotDanhGiaDrl> findByTrangThaiOrderByThoiGianTaoDesc(String trangThai);

    Optional<DotDanhGiaDrl> findFirstByTrangThaiOrderByThoiGianTaoDesc(String trangThai);
}
