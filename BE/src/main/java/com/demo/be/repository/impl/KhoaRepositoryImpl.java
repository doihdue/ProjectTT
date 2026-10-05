package com.demo.be.repository.impl;

import com.demo.be.model.Khoa;
import com.demo.be.repository.KhoaRepositoryCustom;
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
public class KhoaRepositoryImpl implements KhoaRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Khoa> searchAndFilter(String keyword, Pageable pageable) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (keyword != null && !keyword.trim().isBlank()) {
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            whereClause.append("AND (")
                    .append("LOWER(k.maKhoa) LIKE :kw ")
                    .append("OR LOWER(k.tenKhoa) LIKE :kw ")
                    .append("OR LOWER(k.moTa) LIKE :kw ")
                    .append(") ");
            params.put("kw", kw);
        }

        // 1. Câu truy vấn đếm tổng số bản ghi qua EntityManager
        String countJpql = "SELECT COUNT(k) FROM Khoa k " + whereClause;
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        params.forEach(countQuery::setParameter);
        Long total = countQuery.getSingleResult();

        if (total == null || total == 0) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 2. Câu truy vấn lấy danh sách dữ liệu theo phân trang qua EntityManager
        String dataJpql = "SELECT k FROM Khoa k "
                + whereClause
                + "ORDER BY k.id DESC";
        TypedQuery<Khoa> dataQuery = entityManager.createQuery(dataJpql, Khoa.class);
        params.forEach(dataQuery::setParameter);

        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        List<Khoa> resultList = dataQuery.getResultList();
        return new PageImpl<>(resultList, pageable, total);
    }
}
