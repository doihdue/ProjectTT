package com.demo.be.service;

import com.demo.be.dto.khoa.KhoaRequest;
import com.demo.be.dto.khoa.KhoaResponse;
import java.util.List;

public interface KhoaService {

    List<KhoaResponse> findAll();

    KhoaResponse findById(Long id);

    KhoaResponse create(KhoaRequest request);

    KhoaResponse update(Long id, KhoaRequest request);

    void delete(Long id);
}
