package com.demo.be.service.impl;

import com.demo.be.dto.common.PageResponse;
import com.demo.be.dto.lop.LopRequest;
import com.demo.be.dto.lop.LopResponse;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.Khoa;
import com.demo.be.model.Lop;
import com.demo.be.repository.KhoaRepository;
import com.demo.be.repository.LopRepository;
import com.demo.be.service.LopService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LopServiceImpl implements LopService {

    private final LopRepository lopRepository;
    private final KhoaRepository khoaRepository;

    public LopServiceImpl(LopRepository lopRepository, KhoaRepository khoaRepository) {
        this.lopRepository = lopRepository;
        this.khoaRepository = khoaRepository;
    }

    @Override
    public List<LopResponse> findAll() {
        return lopRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public PageResponse<LopResponse> searchAndFilter(String keyword, Long khoaId, int page, int size) {
        int pageNumber = Math.max(0, page);
        int pageSize = (size > 0 && size <= 100) ? size : 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Lop> lopPage = lopRepository.searchAndFilter(keyword, khoaId, pageable);
        List<LopResponse> content = lopPage.getContent().stream().map(this::toResponse).toList();

        return new PageResponse<>(
                content,
                lopPage.getNumber(),
                lopPage.getSize(),
                lopPage.getTotalElements(),
                lopPage.getTotalPages(),
                lopPage.isFirst(),
                lopPage.isLast()
        );
    }

    @Override
    public LopResponse findById(Long id) {
        return toResponse(getLop(id));
    }

    @Override
    public String generateNextMaLop(Long khoaId, String nienKhoa) {
        Khoa khoa = khoaRepository.findById(khoaId)
                .orElseThrow(() -> new ResourceNotFoundException("Khoa not found with id " + khoaId));

        String year = extractYearFromNienKhoa(nienKhoa);
        String maKhoa = (khoa.getMaKhoa() != null && !khoa.getMaKhoa().isBlank())
                ? khoa.getMaKhoa().trim().toUpperCase()
                : "CNTT";
        String prefix = "D" + year + maKhoa;

        List<String> existingMaLops = lopRepository.findMaLopByPrefix(prefix);
        int maxSeq = 0;
        for (String ml : existingMaLops) {
            if (ml != null && ml.startsWith(prefix)) {
                String suffix = ml.substring(prefix.length());
                if (suffix.matches("\\d+")) {
                    try {
                        int seq = Integer.parseInt(suffix);
                        if (seq > maxSeq) {
                            maxSeq = seq;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        int nextSeq = maxSeq + 1;
        return String.format("%s%02d", prefix, nextSeq);
    }

    private String extractYearFromNienKhoa(String nienKhoa) {
        if (nienKhoa != null && !nienKhoa.isBlank()) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d{4}").matcher(nienKhoa);
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return String.valueOf(java.time.LocalDate.now().getYear());
    }

    private void validateNienKhoa(String nienKhoa) {
        if (nienKhoa == null || nienKhoa.isBlank()) {
            throw new IllegalArgumentException("Niên khóa không được để trống.");
        }
        String trimmed = nienKhoa.trim();
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("^(\\d{4})\\s*[-–—]\\s*(\\d{4})$").matcher(trimmed);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Niên khóa phải có định dạng YYYY-YYYY (ví dụ: 2022-2026).");
        }
        int startYear = Integer.parseInt(matcher.group(1));
        int endYear = Integer.parseInt(matcher.group(2));
        if (startYear >= endYear) {
            throw new IllegalArgumentException("Niên khóa không hợp lệ: Năm bắt đầu (" + startYear + ") phải nhỏ hơn năm kết thúc (" + endYear + "). Ví dụ: " + startYear + "-" + (startYear + 4));
        }
        if (endYear - startYear > 10) {
            throw new IllegalArgumentException("Niên khóa không hợp lệ: Thời gian đào tạo không vượt quá 10 năm.");
        }
    }

    @Override
    public LopResponse create(LopRequest request) {
        validateNienKhoa(request.nienKhoa());
        Lop lop = new Lop();
        apply(request, lop);
        if (lop.getMaLop() == null || lop.getMaLop().isBlank()) {
            lop.setMaLop(generateNextMaLop(request.khoaId(), request.nienKhoa()));
        }
        if (lop.getMaLop() != null && lopRepository.findByMaLop(lop.getMaLop().trim()).isPresent()) {
            throw new IllegalArgumentException("Mã lớp '" + lop.getMaLop().trim() + "' đã tồn tại trong hệ thống.");
        }
        return toResponse(lopRepository.save(lop));
    }

    @Override
    public LopResponse update(Long id, LopRequest request) {
        validateNienKhoa(request.nienKhoa());
        Lop lop = getLop(id);
        if (request.maLop() != null && !request.maLop().isBlank()) {
            String trimmedMaLop = request.maLop().trim();
            lopRepository.findByMaLop(trimmedMaLop).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Mã lớp '" + trimmedMaLop + "' đã tồn tại trong hệ thống.");
                }
            });
        }
        apply(request, lop);
        return toResponse(lopRepository.save(lop));
    }

    @Override
    public void delete(Long id) {
        lopRepository.delete(getLop(id));
    }

    private Lop getLop(Long id) {
        return lopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lop not found with id " + id));
    }

    private void apply(LopRequest request, Lop lop) {
        Khoa khoa = khoaRepository.findById(request.khoaId())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa not found with id " + request.khoaId()));
        if (request.maLop() != null && !request.maLop().isBlank()) {
            lop.setMaLop(request.maLop().trim());
        }
        lop.setTenLop(request.tenLop());
        lop.setNienKhoa(request.nienKhoa());
        lop.setSiSoToiDa(request.siSoToiDa());
        lop.setKhoa(khoa);
        lop.setActive(request.active() == null || request.active());
    }

    private LopResponse toResponse(Lop lop) {
        Khoa khoa = lop.getKhoa();
        return new LopResponse(
                lop.getId(),
                lop.getMaLop(),
                lop.getTenLop(),
                lop.getNienKhoa(),
                lop.getSiSoToiDa(),
                khoa != null ? khoa.getId() : null,
                khoa != null ? khoa.getTenKhoa() : null,
                khoa != null ? khoa.getMaKhoa() : null,
                lop.isActive()
        );
    }
}
