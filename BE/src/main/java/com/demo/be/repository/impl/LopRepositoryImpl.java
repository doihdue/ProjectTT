package com.demo.be.repository.impl;

import com.demo.be.model.Lop;
import com.demo.be.repository.LopRepositoryCustom;
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
public class LopRepositoryImpl implements LopRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Lop> searchAndFilter(String keyword, Long khoaId, Pageable pageable) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (keyword != null && !keyword.trim().isBlank()) {
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            whereClause.append("AND (")
                    .append("LOWER(l.maLop) LIKE :kw ")
                    .append("OR LOWER(l.tenLop) LIKE :kw ")
                    .append("OR LOWER(l.nienKhoa) LIKE :kw ")
                    .append("OR LOWER(k.tenKhoa) LIKE :kw ")
                    .append(") ");
            params.put("kw", kw);
        }

        if (khoaId != null) {
            whereClause.append("AND k.id = :khoaId ");
            params.put("khoaId", khoaId);
        }

        // 1. Câu truy vấn đếm tổng số bản ghi (Count Query) qua EntityManager
        String countJpql = "SELECT COUNT(l) FROM Lop l LEFT JOIN l.khoa k " + whereClause;
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        params.forEach(countQuery::setParameter);
        Long total = countQuery.getSingleResult();

        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 2. Câu truy vấn lấy danh sách dữ liệu theo phân trang qua EntityManager
        String dataJpql = "SELECT l FROM Lop l LEFT JOIN FETCH l.khoa k "
                + whereClause
                + "ORDER BY l.id DESC";
        TypedQuery<Lop> dataQuery = entityManager.createQuery(dataJpql, Lop.class);
        params.forEach(dataQuery::setParameter);

        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        List<Lop> resultList = dataQuery.getResultList();
        return new PageImpl<>(resultList, pageable, total);
    }
}
