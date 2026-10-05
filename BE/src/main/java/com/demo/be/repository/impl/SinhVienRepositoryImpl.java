package com.demo.be.repository.impl;

import com.demo.be.model.SinhVien;
import com.demo.be.repository.SinhVienRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SinhVienRepositoryImpl implements SinhVienRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<SinhVien> searchAndFilter(String keyword, Long lopId, String gioiTinh, Pageable pageable) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (keyword != null && !keyword.trim().isBlank()) {
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            whereClause.append("AND (")
                    .append("LOWER(s.mssv) LIKE :kw ")
                    .append("OR LOWER(s.hoTen) LIKE :kw ")
                    .append("OR LOWER(s.email) LIKE :kw ")
                    .append("OR s.soDienThoai LIKE :kw ")
                    .append("OR LOWER(l.tenLop) LIKE :kw ")
                    .append("OR LOWER(k.tenKhoa) LIKE :kw ")
                    .append(") ");
            params.put("kw", kw);
        }

        if (lopId != null) {
            whereClause.append("AND s.lop.id = :lopId ");
            params.put("lopId", lopId);
        }

        if (gioiTinh != null && !gioiTinh.trim().isBlank()) {
            whereClause.append("AND s.gioiTinh = :gioiTinh ");
            params.put("gioiTinh", gioiTinh.trim());
        }

        // 1. Câu truy vấn đếm tổng số bản ghi (Count Query) qua EntityManager
        String countJpql = "SELECT COUNT(s) FROM SinhVien s LEFT JOIN s.lop l LEFT JOIN l.khoa k " + whereClause;
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        params.forEach(countQuery::setParameter);
        Long total = countQuery.getSingleResult();

        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 2. Câu truy vấn lấy danh sách dữ liệu theo phân trang qua EntityManager
        // Sử dụng FETCH JOIN để eager load lop và khoa, tối ưu hiệu năng và tránh lỗi N+1
        String dataJpql = "SELECT s FROM SinhVien s LEFT JOIN FETCH s.lop l LEFT JOIN FETCH l.khoa k "
                + whereClause
                + "ORDER BY s.id DESC";
        TypedQuery<SinhVien> dataQuery = entityManager.createQuery(dataJpql, SinhVien.class);
        params.forEach(dataQuery::setParameter);

        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        List<SinhVien> resultList = dataQuery.getResultList();
        return new PageImpl<>(resultList, pageable, total);
    }
}
