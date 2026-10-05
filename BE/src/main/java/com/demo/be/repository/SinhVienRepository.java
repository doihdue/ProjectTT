package com.demo.be.repository;

import com.demo.be.model.SinhVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SinhVienRepository extends JpaRepository<SinhVien, Long>, SinhVienRepositoryCustom {
    Optional<SinhVien> findByMssv(String mssv);
    List<SinhVien> findByLop_Id(Long lopId);
    List<SinhVien> findByLop_Khoa_Id(Long khoaId);
    long countByActiveTrue();

    @Query("SELECT s.mssv FROM SinhVien s WHERE s.mssv LIKE CONCAT(:prefix, '%')")
    List<String> findMssvByPrefix1(@Param("prefix") String prefix);

    @Query(value = " select s.mssv from sinh_vien s where s.mssv like CONCAT(:prefix, '%')", nativeQuery = true)
    List<String> findMssvByPrefix(@Param("prefix") String prefix);
}
