package com.demo.be.service.impl;

import com.demo.be.dto.diemrenluyen.DiemRenLuyenDuyetRequest;
import com.demo.be.dto.diemrenluyen.DiemRenLuyenResponse;
import com.demo.be.dto.diemrenluyen.DiemRenLuyenSubmitRequest;
import com.demo.be.dto.diemrenluyen.DrlThongKeResponse;
import com.demo.be.dto.message.NotificationEventMessage;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.DiemRenLuyen;
import com.demo.be.model.DotDanhGiaDrl;
import com.demo.be.model.Khoa;
import com.demo.be.model.Lop;
import com.demo.be.model.SinhVien;
import com.demo.be.producer.NotificationProducer;
import com.demo.be.repository.DiemRenLuyenRepository;
import com.demo.be.repository.DotDanhGiaRepository;
import com.demo.be.repository.SinhVienRepository;
import com.demo.be.service.DiemRenLuyenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiemRenLuyenServiceImpl implements DiemRenLuyenService {

    private final DiemRenLuyenRepository diemRenLuyenRepository;
    private final SinhVienRepository sinhVienRepository;
    private final DotDanhGiaRepository dotDanhGiaRepository;
    private final NotificationProducer notificationProducer;

    @Override
    @Transactional
    public DiemRenLuyenResponse submitDrl(String mssv, DiemRenLuyenSubmitRequest request) {
        SinhVien sv = sinhVienRepository.findByMssv(mssv)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên có MSSV: " + mssv));

        DotDanhGiaDrl dot;
        if (request.dotDanhGiaId() != null) {
            dot = dotDanhGiaRepository.findById(request.dotDanhGiaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt đánh giá với ID: " + request.dotDanhGiaId()));
        } else {
            dot = dotDanhGiaRepository.findFirstByTrangThaiOrderByThoiGianTaoDesc("DANG_MO")
                    .orElseThrow(() -> new IllegalArgumentException("Hiện tại Nhà trường chưa mở đợt đánh giá điểm rèn luyện hoặc các đợt đã đóng!"));
        }

        if (!"DANG_MO".equalsIgnoreCase(dot.getTrangThai())) {
            throw new IllegalArgumentException("Đợt đánh giá \"" + dot.getTenDot() + "\" hiện đang đóng. Bạn chỉ có thể nộp đánh giá khi Admin mở đợt!");
        }

        java.time.LocalDate now = java.time.LocalDate.now();
        if (dot.getNgayBatDau() != null && now.isBefore(dot.getNgayBatDau())) {
            throw new IllegalArgumentException("Đợt đánh giá \"" + dot.getTenDot() + "\" chưa đến thời gian mở (từ ngày " + dot.getNgayBatDau() + ")!");
        }
        if (dot.getNgayKetThuc() != null && now.isAfter(dot.getNgayKetThuc())) {
            throw new IllegalArgumentException("Đợt đánh giá \"" + dot.getTenDot() + "\" đã hết hạn nộp (hạn đến " + dot.getNgayKetThuc() + ")!");
        }

        int tongDiemSv = request.tieuChi1Sv() + request.tieuChi2Sv() + request.tieuChi3Sv()
                + request.tieuChi4Sv() + request.tieuChi5Sv();
        String xepLoaiTamTinh = DiemRenLuyenService.xepLoaiDrl(tongDiemSv);

        DiemRenLuyen drl = diemRenLuyenRepository.findBySinhVien_MssvAndDotDanhGia_Id(mssv, dot.getId())
                .orElse(null);

        if (drl == null && dot.getHocKy() != null && dot.getNamHoc() != null) {
            drl = diemRenLuyenRepository.findBySinhVien_MssvAndHocKyAndNamHoc(mssv, dot.getHocKy(), dot.getNamHoc())
                    .orElse(null);
        }

        if (drl != null) {
            if ("DA_DUYET".equalsIgnoreCase(drl.getTrangThai())) {
                throw new IllegalArgumentException("Bản đánh giá điểm rèn luyện đợt \"" + dot.getTenDot() + "\" đã được phê duyệt, không thể nộp lại!");
            }
            drl.setDotDanhGia(dot);
            drl.setHocKy(dot.getHocKy());
            drl.setNamHoc(dot.getNamHoc());
            drl.setTieuChi1Sv(request.tieuChi1Sv());
            drl.setTieuChi2Sv(request.tieuChi2Sv());
            drl.setTieuChi3Sv(request.tieuChi3Sv());
            drl.setTieuChi4Sv(request.tieuChi4Sv());
            drl.setTieuChi5Sv(request.tieuChi5Sv());
            drl.setTongDiemSv(tongDiemSv);
            drl.setXepLoai(xepLoaiTamTinh);
            drl.setGhiChuSinhVien(request.ghiChuSinhVien());
            drl.setMinhChung(request.minhChung());
            drl.setTrangThai("CHO_DUYET");
            drl.setNgayNop(LocalDateTime.now());
            drl.setNhanXetAdmin(null);
        } else {
            drl = DiemRenLuyen.builder()
                    .sinhVien(sv)
                    .dotDanhGia(dot)
                    .hocKy(dot.getHocKy())
                    .namHoc(dot.getNamHoc())
                    .tieuChi1Sv(request.tieuChi1Sv())
                    .tieuChi2Sv(request.tieuChi2Sv())
                    .tieuChi3Sv(request.tieuChi3Sv())
                    .tieuChi4Sv(request.tieuChi4Sv())
                    .tieuChi5Sv(request.tieuChi5Sv())
                    .tongDiemSv(tongDiemSv)
                    .xepLoai(xepLoaiTamTinh)
                    .ghiChuSinhVien(request.ghiChuSinhVien())
                    .minhChung(request.minhChung())
                    .trangThai("CHO_DUYET")
                    .ngayNop(LocalDateTime.now())
                    .build();
        }

        DiemRenLuyen saved = diemRenLuyenRepository.save(drl);

        try {
            NotificationEventMessage msg = NotificationEventMessage.builder()
                    .eventType("DRL_SUBMITTED")
                    .mssv(sv.getMssv())
                    .hoTen(sv.getHoTen())
                    .hocKy(dot.getHocKy())
                    .namHoc(dot.getNamHoc())
                    .tongDiem(tongDiemSv)
                    .xepLoai(xepLoaiTamTinh)
                    .recipient("admin")
                    .link("/quan-ly/diem-ren-luyen")
                    .build();
            notificationProducer.sendNotification(msg);
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo sự kiện DRL_SUBMITTED: {}", e.getMessage());
        }

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiemRenLuyenResponse> getMyDrlList(String mssv) {
        return diemRenLuyenRepository.findBySinhVien_MssvOrderByNgayNopDesc(mssv)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiemRenLuyenResponse> getAllForAdmin(Long dotDanhGiaId, String trangThai, Integer hocKy, String namHoc, Long khoaId, Long lopId, String keyword) {
        return diemRenLuyenRepository.findByFilters(dotDanhGiaId, trangThai, hocKy, namHoc, khoaId, lopId, keyword)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DiemRenLuyenResponse getById(Long id) {
        return toResponse(getDrl(id));
    }

    @Override
    @Transactional
    public DiemRenLuyenResponse duyetDrl(Long id, DiemRenLuyenDuyetRequest request, String adminUsername) {
        DiemRenLuyen drl = getDrl(id);
        SinhVien sv = drl.getSinhVien();

        if (Boolean.TRUE.equals(request.chapNhan())) {
            int tc1 = request.tieuChi1Admin() != null ? request.tieuChi1Admin() : drl.getTieuChi1Sv();
            int tc2 = request.tieuChi2Admin() != null ? request.tieuChi2Admin() : drl.getTieuChi2Sv();
            int tc3 = request.tieuChi3Admin() != null ? request.tieuChi3Admin() : drl.getTieuChi3Sv();
            int tc4 = request.tieuChi4Admin() != null ? request.tieuChi4Admin() : drl.getTieuChi4Sv();
            int tc5 = request.tieuChi5Admin() != null ? request.tieuChi5Admin() : drl.getTieuChi5Sv();

            int tongAdmin = tc1 + tc2 + tc3 + tc4 + tc5;
            String xepLoai = DiemRenLuyenService.xepLoaiDrl(tongAdmin);

            drl.setTieuChi1Admin(tc1);
            drl.setTieuChi2Admin(tc2);
            drl.setTieuChi3Admin(tc3);
            drl.setTieuChi4Admin(tc4);
            drl.setTieuChi5Admin(tc5);
            drl.setTongDiemAdmin(tongAdmin);
            drl.setXepLoai(xepLoai);
            drl.setTrangThai("DA_DUYET");
            drl.setNhanXetAdmin(request.nhanXetAdmin());
            drl.setNgayDuyet(LocalDateTime.now());
            drl.setNguoiDuyet(adminUsername);

            DiemRenLuyen saved = diemRenLuyenRepository.save(drl);

            try {
                NotificationEventMessage msg = NotificationEventMessage.builder()
                        .eventType("DRL_APPROVED")
                        .mssv(sv.getMssv())
                        .hoTen(sv.getHoTen())
                        .hocKy(drl.getHocKy())
                        .namHoc(drl.getNamHoc())
                        .tongDiem(tongAdmin)
                        .xepLoai(xepLoai)
                        .reason(request.nhanXetAdmin())
                        .nguoiDuyet(adminUsername)
                        .recipient(sv.getMssv())
                        .link("/sinh-vien/ket-qua-drl")
                        .build();
                notificationProducer.sendNotification(msg);
            } catch (Exception e) {
                log.error("Lỗi khi gửi thông báo sự kiện DRL_APPROVED: {}", e.getMessage());
            }

            return toResponse(saved);
        } else {
            drl.setTrangThai("TU_CHOI");
            drl.setNhanXetAdmin(request.nhanXetAdmin() != null ? request.nhanXetAdmin() : "Vui lòng xem lại và bổ sung minh chứng");
            drl.setNgayDuyet(LocalDateTime.now());
            drl.setNguoiDuyet(adminUsername);

            DiemRenLuyen saved = diemRenLuyenRepository.save(drl);

            try {
                NotificationEventMessage msg = NotificationEventMessage.builder()
                        .eventType("DRL_REJECTED")
                        .mssv(sv.getMssv())
                        .hoTen(sv.getHoTen())
                        .hocKy(drl.getHocKy())
                        .namHoc(drl.getNamHoc())
                        .reason(drl.getNhanXetAdmin())
                        .nguoiDuyet(adminUsername)
                        .recipient(sv.getMssv())
                        .link("/sinh-vien/danh-gia-drl")
                        .build();
                notificationProducer.sendNotification(msg);
            } catch (Exception e) {
                log.error("Lỗi khi gửi thông báo sự kiện DRL_REJECTED: {}", e.getMessage());
            }

            return toResponse(saved);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        diemRenLuyenRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DrlThongKeResponse getThongKe() {
        long tong = diemRenLuyenRepository.count();
        long choDuyet = diemRenLuyenRepository.countByTrangThai("CHO_DUYET");
        long daDuyet = diemRenLuyenRepository.countByTrangThai("DA_DUYET");
        long tuChoi = diemRenLuyenRepository.countByTrangThai("TU_CHOI");

        Map<String, Long> phanBo = new HashMap<>();
        phanBo.put("Xuất sắc", diemRenLuyenRepository.countByXepLoai("Xuất sắc"));
        phanBo.put("Tốt", diemRenLuyenRepository.countByXepLoai("Tốt"));
        phanBo.put("Khá", diemRenLuyenRepository.countByXepLoai("Khá"));
        phanBo.put("Trung bình", diemRenLuyenRepository.countByXepLoai("Trung bình"));
        phanBo.put("Yếu", diemRenLuyenRepository.countByXepLoai("Yếu"));
        phanBo.put("Kém", diemRenLuyenRepository.countByXepLoai("Kém"));

        return new DrlThongKeResponse(tong, choDuyet, daDuyet, tuChoi, phanBo);
    }

    private DiemRenLuyen getDrl(Long id) {
        return diemRenLuyenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy kết quả điểm rèn luyện với ID: " + id));
    }

    private DiemRenLuyenResponse toResponse(DiemRenLuyen drl) {
        SinhVien sv = drl.getSinhVien();
        Lop lop = sv != null ? sv.getLop() : null;
        Khoa khoa = lop != null ? lop.getKhoa() : null;
        DotDanhGiaDrl dot = drl.getDotDanhGia();

        String xepLoai = drl.getXepLoai();
        if (xepLoai == null || xepLoai.contains("?") || "T?t".equalsIgnoreCase(xepLoai.trim())) {
            Integer score = drl.getTongDiemAdmin() != null ? drl.getTongDiemAdmin() : drl.getTongDiemSv();
            xepLoai = score != null ? DiemRenLuyenService.xepLoaiDrl(score) : "Tốt";
        }

        return new DiemRenLuyenResponse(
                drl.getId(),
                sv != null ? sv.getId() : null,
                sv != null ? sv.getMssv() : null,
                sv != null ? sv.getHoTen() : null,
                sv != null ? sv.getEmail() : null,
                lop != null ? lop.getId() : null,
                lop != null ? lop.getTenLop() : null,
                khoa != null ? khoa.getId() : null,
                khoa != null ? khoa.getTenKhoa() : null,
                dot != null ? dot.getId() : null,
                dot != null ? dot.getTenDot() : ("Đánh giá ĐRL Học kỳ " + drl.getHocKy() + " (" + drl.getNamHoc() + ")"),
                drl.getHocKy(),
                drl.getNamHoc(),
                drl.getTieuChi1Sv(),
                drl.getTieuChi2Sv(),
                drl.getTieuChi3Sv(),
                drl.getTieuChi4Sv(),
                drl.getTieuChi5Sv(),
                drl.getTongDiemSv(),
                drl.getTieuChi1Admin(),
                drl.getTieuChi2Admin(),
                drl.getTieuChi3Admin(),
                drl.getTieuChi4Admin(),
                drl.getTieuChi5Admin(),
                drl.getTongDiemAdmin(),
                xepLoai,
                drl.getGhiChuSinhVien(),
                drl.getMinhChung(),
                drl.getNhanXetAdmin(),
                drl.getTrangThai(),
                drl.getNgayNop(),
                drl.getNgayDuyet(),
                drl.getNguoiDuyet()
        );
    }
}
