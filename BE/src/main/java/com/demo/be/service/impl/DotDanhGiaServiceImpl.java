package com.demo.be.service.impl;

import com.demo.be.dto.diemrenluyen.DotDanhGiaRequest;
import com.demo.be.dto.diemrenluyen.DotDanhGiaResponse;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.DotDanhGiaDrl;
import com.demo.be.repository.DiemRenLuyenRepository;
import com.demo.be.repository.DotDanhGiaRepository;
import com.demo.be.service.DotDanhGiaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DotDanhGiaServiceImpl implements DotDanhGiaService {

    private final DotDanhGiaRepository dotDanhGiaRepository;
    private final DiemRenLuyenRepository diemRenLuyenRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DotDanhGiaResponse> getAllDots() {
        return dotDanhGiaRepository.findAllByOrderByThoiGianTaoDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DotDanhGiaResponse> getOpenDots() {
        return dotDanhGiaRepository.findByTrangThaiOrderByThoiGianTaoDesc("DANG_MO")
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DotDanhGiaResponse getById(Long id) {
        return toResponse(getDot(id));
    }

    private String resolveTenDot(String rawTenDot, Integer hocKy, String namHoc) {
        String cleanNamHoc = (namHoc != null) ? namHoc.trim() : "";
        int hk = (hocKy != null && hocKy >= 1) ? hocKy : 1;
        if (rawTenDot == null || rawTenDot.isBlank()) {
            return "Đánh giá ĐRL Học kỳ " + hk + " (" + cleanNamHoc + ")";
        }
        String trimmed = rawTenDot.trim();
        if (trimmed.contains("Học kỳ") && !trimmed.contains("Học kỳ " + hk)) {
            return trimmed.replaceAll("Học kỳ\\s*\\d+", "Học kỳ " + hk);
        }
        return trimmed;
    }

    @Override
    @Transactional
    public DotDanhGiaResponse createDot(DotDanhGiaRequest request) {
        String tenDot = resolveTenDot(request.tenDot(), request.hocKy(), request.namHoc());
        DotDanhGiaDrl dot = DotDanhGiaDrl.builder()
                .tenDot(tenDot)
                .hocKy(request.hocKy())
                .namHoc(request.namHoc() != null ? request.namHoc().trim() : "")
                .ngayBatDau(request.ngayBatDau())
                .ngayKetThuc(request.ngayKetThuc())
                .trangThai(request.trangThai() != null && !request.trangThai().isBlank() ? request.trangThai().trim() : "DANG_MO")
                .ghiChu(request.ghiChu() != null ? request.ghiChu().trim() : null)
                .thoiGianTao(LocalDateTime.now())
                .build();

        DotDanhGiaDrl saved = dotDanhGiaRepository.save(dot);
        log.info("Đã tạo đợt đánh giá ĐRL mới: id={}, tenDot={}", saved.getId(), saved.getTenDot());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DotDanhGiaResponse updateDot(Long id, DotDanhGiaRequest request) {
        DotDanhGiaDrl dot = getDot(id);
        String tenDot = resolveTenDot(request.tenDot(), request.hocKy(), request.namHoc());
        dot.setTenDot(tenDot);
        dot.setHocKy(request.hocKy());
        dot.setNamHoc(request.namHoc() != null ? request.namHoc().trim() : "");
        dot.setNgayBatDau(request.ngayBatDau());
        dot.setNgayKetThuc(request.ngayKetThuc());
        if (request.trangThai() != null && !request.trangThai().isBlank()) {
            dot.setTrangThai(request.trangThai().trim());
        }
        dot.setGhiChu(request.ghiChu() != null ? request.ghiChu().trim() : null);

        DotDanhGiaDrl saved = dotDanhGiaRepository.save(dot);
        log.info("Đã cập nhật đợt đánh giá ĐRL: id={}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DotDanhGiaResponse toggleTrangThai(Long id) {
        DotDanhGiaDrl dot = getDot(id);
        if ("DANG_MO".equalsIgnoreCase(dot.getTrangThai())) {
            dot.setTrangThai("DONG");
        } else {
            dot.setTrangThai("DANG_MO");
        }
        DotDanhGiaDrl saved = dotDanhGiaRepository.save(dot);
        log.info("Chuyển trạng thái đợt đánh giá id={} sang {}", saved.getId(), saved.getTrangThai());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDot(Long id) {
        DotDanhGiaDrl dot = getDot(id);
        long count = diemRenLuyenRepository.countByDotDanhGia_Id(id);
        if (count > 0) {
            throw new IllegalArgumentException("Đợt đánh giá \"" + dot.getTenDot() + "\" đã có " + count
                    + " sinh viên nộp phiếu! Bạn không thể xóa đợt này. Hãy chuyển sang trạng thái Đóng đợt.");
        }
        dotDanhGiaRepository.delete(dot);
        log.info("Đã xóa đợt đánh giá ĐRL id={}", id);
    }

    @Override
    public DotDanhGiaDrl getDot(Long id) {
        return dotDanhGiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đánh giá ĐRL với ID: " + id));
    }

    private DotDanhGiaResponse toResponse(DotDanhGiaDrl dot) {
        long count = diemRenLuyenRepository.countByDotDanhGia_Id(dot.getId());
        return new DotDanhGiaResponse(
                dot.getId(),
                dot.getTenDot(),
                dot.getHocKy(),
                dot.getNamHoc(),
                dot.getNgayBatDau(),
                dot.getNgayKetThuc(),
                dot.getTrangThai(),
                dot.getGhiChu(),
                dot.getThoiGianTao(),
                count
        );
    }
}
