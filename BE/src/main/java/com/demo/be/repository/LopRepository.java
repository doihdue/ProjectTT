package com.demo.be.repository;

import com.demo.be.model.Lop;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LopRepository extends JpaRepository<Lop, Long> {
    Optional<Lop> findByMaLop(String maLop);

    @Query("SELECT l.maLop FROM Lop l WHERE l.maLop LIKE CONCAT(:prefix, '%')")
    List<String> findMaLopByPrefix(@Param("prefix") String prefix);
}
