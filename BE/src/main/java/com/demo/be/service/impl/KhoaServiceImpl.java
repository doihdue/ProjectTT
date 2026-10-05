package com.demo.be.service.impl;

import com.demo.be.dto.common.PageResponse;
import com.demo.be.dto.khoa.KhoaRequest;
import com.demo.be.dto.khoa.KhoaResponse;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.Khoa;
import com.demo.be.repository.KhoaRepository;
import com.demo.be.service.KhoaService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public PageResponse<KhoaResponse> searchAndFilter(String keyword, int page, int size) {
        int pageNumber = Math.max(0, page);
        int pageSize = (size > 0 && size <= 100) ? size : 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Khoa> khoaPage = khoaRepository.searchAndFilter(keyword, pageable);
        List<KhoaResponse> content = khoaPage.getContent().stream().map(this::toResponse).toList();

        return new PageResponse<>(
                content,
                khoaPage.getNumber(),
                khoaPage.getSize(),
                khoaPage.getTotalElements(),
                khoaPage.getTotalPages(),
                khoaPage.isFirst(),
                khoaPage.isLast()
        );
    }

    @Override
    public KhoaResponse findById(Long id) {
        return toResponse(getKhoa(id));
    }

    @Override
    public KhoaResponse create(KhoaRequest request) {
        if (request.maKhoa() != null && !request.maKhoa().isBlank()) {
            String trimmedMaKhoa = request.maKhoa().trim();
            if (khoaRepository.findByMaKhoa(trimmedMaKhoa).isPresent()) {
                throw new IllegalArgumentException("Mã khoa '" + trimmedMaKhoa + "' đã tồn tại trong hệ thống. Vui lòng chọn mã khoa khác.");
            }
        }
        Khoa khoa = new Khoa();
        apply(request, khoa);
        return toResponse(khoaRepository.save(khoa));
    }

    @Override
    public KhoaResponse update(Long id, KhoaRequest request) {
        Khoa khoa = getKhoa(id);
        if (request.maKhoa() != null && !request.maKhoa().isBlank()) {
            String trimmedMaKhoa = request.maKhoa().trim();
            khoaRepository.findByMaKhoa(trimmedMaKhoa).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Mã khoa '" + trimmedMaKhoa + "' đã tồn tại trong hệ thống. Vui lòng chọn mã khoa khác.");
                }
            });
        }
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
