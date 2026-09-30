package com.demo.be.service.impl;

import com.demo.be.dto.khoa.KhoaRequest;
import com.demo.be.dto.khoa.KhoaResponse;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.Khoa;
import com.demo.be.repository.KhoaRepository;
import com.demo.be.service.KhoaService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class KhoaServiceImpl implements KhoaService {

    private final KhoaRepository khoaRepository;

    public KhoaServiceImpl(KhoaRepository khoaRepository) {
        this.khoaRepository = khoaRepository;
    }

    @Override
    public List<KhoaResponse> findAll() {
        return khoaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public KhoaResponse findById(Long id) {
        return toResponse(getKhoa(id));
    }

    @Override
    public KhoaResponse create(KhoaRequest request) {
        Khoa khoa = new Khoa();
        apply(request, khoa);
        return toResponse(khoaRepository.save(khoa));
    }

    @Override
    public KhoaResponse update(Long id, KhoaRequest request) {
        Khoa khoa = getKhoa(id);
        apply(request, khoa);
        return toResponse(khoaRepository.save(khoa));
    }

    @Override
    public void delete(Long id) {
        Khoa khoa = getKhoa(id);
        khoaRepository.delete(khoa);
    }

    private Khoa getKhoa(Long id) {
        return khoaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khoa not found with id " + id));
    }

    private void apply(KhoaRequest request, Khoa khoa) {
        khoa.setMaKhoa(request.maKhoa());
        khoa.setTenKhoa(request.tenKhoa());
        khoa.setMoTa(request.moTa());
        khoa.setActive(request.active() == null || request.active());
    }

    private KhoaResponse toResponse(Khoa khoa) {
        return new KhoaResponse(khoa.getId(), khoa.getMaKhoa(), khoa.getTenKhoa(), khoa.getMoTa(), khoa.isActive());
    }
}
