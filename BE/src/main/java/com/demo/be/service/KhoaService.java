package com.demo.be.service;

import com.demo.be.dto.common.PageResponse;
import com.demo.be.dto.khoa.KhoaRequest;
import com.demo.be.dto.khoa.KhoaResponse;
import java.util.List;

public interface KhoaService {

    List<KhoaResponse> findAll();

    PageResponse<KhoaResponse> searchAndFilter(String keyword, int page, int size);

    KhoaResponse findById(Long id);

    KhoaResponse create(KhoaRequest request);

    KhoaResponse update(Long id, KhoaRequest request);

    void delete(Long id);
}
