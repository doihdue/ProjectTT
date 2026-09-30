package com.demo.be.service.impl;

import com.demo.be.dto.report.DrlReportRowDto;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.DiemRenLuyen;
import com.demo.be.model.DotDanhGiaDrl;
import com.demo.be.model.Khoa;
import com.demo.be.model.Lop;
import com.demo.be.model.SinhVien;
import com.demo.be.repository.DiemRenLuyenRepository;
import com.demo.be.repository.DotDanhGiaRepository;
import com.demo.be.repository.KhoaRepository;
import com.demo.be.repository.LopRepository;
import com.demo.be.service.DiemRenLuyenService;
import com.demo.be.service.JasperReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class JasperReportServiceImpl implements JasperReportService {

    private final DiemRenLuyenRepository diemRenLuyenRepository;
    private final DotDanhGiaRepository dotDanhGiaRepository;
    private final KhoaRepository khoaRepository;
    private final LopRepository lopRepository;

    private final Map<String, JasperReport> reportCache = new ConcurrentHashMap<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Lấy mẫu báo cáo Jasper đã biên dịch và lưu trong cache để tối ưu hiệu năng
     */
    private JasperReport getCompiledReport(String reportResourcePath) {
        return reportCache.computeIfAbsent(reportResourcePath, path -> {
            try {
                ClassPathResource resource = new ClassPathResource(path);
                try (InputStream is = resource.getInputStream()) {
                    log.info("Đang biên dịch mẫu báo cáo JasperReports: {}", path);
                    return JasperCompileManager.compileReport(is);
                }
            } catch (Exception e) {
                log.error("Lỗi khi tải và biên dịch mẫu JRXML: {}", path, e);
                throw new RuntimeException("Không thể biên dịch mẫu báo cáo Jasper: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Xuất phiếu đánh giá điểm rèn luyện cá nhân của sinh viên ra PDF
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] exportPhieuDrlPdf(Long drlId) {
        DiemRenLuyen drl = diemRenLuyenRepository.findById(drlId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu điểm rèn luyện với ID: " + drlId));

        SinhVien sv = drl.getSinhVien();
        Lop lop = sv != null ? sv.getLop() : null;
        Khoa khoa = lop != null ? lop.getKhoa() : null;
        DotDanhGiaDrl dot = drl.getDotDanhGia();

        LocalDate now = LocalDate.now();

        String xepLoai = drl.getXepLoai();
        if (xepLoai == null || xepLoai.contains("?") || "T?t".equalsIgnoreCase(xepLoai.trim())) {
            Integer score = drl.getTongDiemAdmin() != null ? drl.getTongDiemAdmin() : drl.getTongDiemSv();
            xepLoai = score != null ? DiemRenLuyenService.xepLoaiDrl(score) : "Tốt";
        }

        Map<String, Object> params = new HashMap<>();
        params.put("TEN_TRUONG", "TRƯỜNG ĐẠI HỌC CÔNG NGHỆ");
        params.put("TEN_KHOA", khoa != null ? khoa.getTenKhoa() : "---");
        params.put("TEN_LOP", lop != null ? lop.getTenLop() : "---");
        params.put("MSSV", sv != null ? sv.getMssv() : "---");
        params.put("HO_TEN", sv != null ? sv.getHoTen() : "---");
        params.put("NGAY_SINH", (sv != null && sv.getNgaySinh() != null) ? sv.getNgaySinh().format(DATE_FORMATTER) : "---");
        params.put("GIOI_TINH", (sv != null && sv.getGioiTinh() != null) ? sv.getGioiTinh() : "---");

        String tenDot = dot != null ? dot.getTenDot() : ("Đánh giá ĐRL Học kỳ " + drl.getHocKy() + " (" + drl.getNamHoc() + ")");
        params.put("TEN_DOT", tenDot);
        params.put("HOC_KY", String.valueOf(drl.getHocKy()));
        params.put("NAM_HOC", drl.getNamHoc());

        params.put("TC1_SV", drl.getTieuChi1Sv());
        params.put("TC2_SV", drl.getTieuChi2Sv());
        params.put("TC3_SV", drl.getTieuChi3Sv());
        params.put("TC4_SV", drl.getTieuChi4Sv());
        params.put("TC5_SV", drl.getTieuChi5Sv());
        params.put("TONG_SV", drl.getTongDiemSv());

        params.put("TC1_ADMIN", drl.getTieuChi1Admin());
        params.put("TC2_ADMIN", drl.getTieuChi2Admin());
        params.put("TC3_ADMIN", drl.getTieuChi3Admin());
        params.put("TC4_ADMIN", drl.getTieuChi4Admin());
        params.put("TC5_ADMIN", drl.getTieuChi5Admin());
        params.put("TONG_ADMIN", drl.getTongDiemAdmin());

        params.put("XEP_LOAI", xepLoai);
        params.put("TRANG_THAI", drl.getTrangThai());
        params.put("NGAY_NOP", drl.getNgayNop() != null ? drl.getNgayNop().format(DATE_TIME_FORMATTER) : "---");
        params.put("NGAY_DUYET", drl.getNgayDuyet() != null ? drl.getNgayDuyet().format(DATE_TIME_FORMATTER) : "---");
        params.put("NGUOI_DUYET", (drl.getNguoiDuyet() != null && !drl.getNguoiDuyet().isBlank()) ? drl.getNguoiDuyet() : "Hội đồng đánh giá ĐRL");
        params.put("NHAN_XET_ADMIN", drl.getNhanXetAdmin());
        params.put("NGAY_XUAT", "Ngày " + now.getDayOfMonth() + " tháng " + now.getMonthValue() + " năm " + now.getYear());

        try {
            JasperReport jasperReport = getCompiledReport("reports/Phieu_Diem_Ren_Luyen.jrxml");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (JRException e) {
            log.error("Lỗi khi xuất PDF phiếu ĐRL cho ID: {}", drlId, e);
            throw new RuntimeException("Lỗi xuất PDF phiếu ĐRL: " + e.getMessage(), e);
        }
    }

    /**
     * Xuất bảng tổng hợp kết quả điểm rèn luyện theo các tiêu chí lọc ra PDF
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] exportBangTongHopDrlPdf(
            Long dotDanhGiaId,
            String trangThai,
            Integer hocKy,
            String namHoc,
            Long khoaId,
            Long lopId,
            String keyword,
            String nguoiLap
    ) {
        List<DiemRenLuyen> list = diemRenLuyenRepository.findByFilters(dotDanhGiaId, trangThai, hocKy, namHoc, khoaId, lopId, keyword);

        List<DrlReportRowDto> rows = new ArrayList<>();
        int stt = 1;
        int xuatSac = 0;
        int tot = 0;
        int kha = 0;
        int trungBinh = 0;
        int yeuKem = 0;

        for (DiemRenLuyen d : list) {
            SinhVien sv = d.getSinhVien();
            Lop lop = sv != null ? sv.getLop() : null;
            Khoa khoa = lop != null ? lop.getKhoa() : null;

            String xl = d.getXepLoai();
            if (xl == null || xl.contains("?") || "T?t".equalsIgnoreCase(xl.trim())) {
                Integer s = d.getTongDiemAdmin() != null ? d.getTongDiemAdmin() : d.getTongDiemSv();
                xl = s != null ? DiemRenLuyenService.xepLoaiDrl(s) : "---";
            }

            if ("Xuất sắc".equalsIgnoreCase(xl)) {
                xuatSac++;
            } else if ("Tốt".equalsIgnoreCase(xl)) {
                tot++;
            } else if ("Khá".equalsIgnoreCase(xl)) {
                kha++;
            } else if ("Trung bình".equalsIgnoreCase(xl)) {
                trungBinh++;
            } else if ("Yếu".equalsIgnoreCase(xl) || "Kém".equalsIgnoreCase(xl)) {
                yeuKem++;
            }

            rows.add(DrlReportRowDto.builder()
                    .stt(stt++)
                    .mssv(sv != null ? sv.getMssv() : "---")
                    .hoTen(sv != null ? sv.getHoTen() : "---")
                    .tenLop(lop != null ? lop.getTenLop() : "---")
                    .tenKhoa(khoa != null ? khoa.getTenKhoa() : "---")
                    .tieuChi1Admin(d.getTieuChi1Admin())
                    .tieuChi2Admin(d.getTieuChi2Admin())
                    .tieuChi3Admin(d.getTieuChi3Admin())
                    .tieuChi4Admin(d.getTieuChi4Admin())
                    .tieuChi5Admin(d.getTieuChi5Admin())
                    .tongDiemSv(d.getTongDiemSv())
                    .tongDiemAdmin(d.getTongDiemAdmin())
                    .xepLoai(xl)
                    .trangThai(d.getTrangThai())
                    .build()
            );
        }

        String tenDotStr = "Tất cả";
        if (dotDanhGiaId != null) {
            tenDotStr = dotDanhGiaRepository.findById(dotDanhGiaId).map(DotDanhGiaDrl::getTenDot).orElse("Tất cả");
        }
        String tenKhoaStr = "Tất cả";
        if (khoaId != null) {
            tenKhoaStr = khoaRepository.findById(khoaId).map(Khoa::getTenKhoa).orElse("Tất cả");
        }
        String tenLopStr = "Tất cả";
        if (lopId != null) {
            tenLopStr = lopRepository.findById(lopId).map(Lop::getTenLop).orElse("Tất cả");
        }

        LocalDate now = LocalDate.now();

        Map<String, Object> params = new HashMap<>();
        params.put("TEN_TRUONG", "TRƯỜNG ĐẠI HỌC CÔNG NGHỆ");
        params.put("TIEU_DE", "BẢNG TỔNG HỢP KẾT QUẢ ĐIỂM RÈN LUYỆN");
        params.put("TEN_DOT", tenDotStr);
        params.put("TEN_KHOA", tenKhoaStr);
        params.put("TEN_LOP", tenLopStr);
        params.put("HOC_KY", hocKy != null ? String.valueOf(hocKy) : "Tất cả");
        params.put("NAM_HOC", (namHoc != null && !namHoc.isBlank()) ? namHoc : "Tất cả");
        params.put("TONG_SO_SV", rows.size());
        params.put("SO_XUAT_SAC", xuatSac);
        params.put("SO_TOT", tot);
        params.put("SO_KHA", kha);
        params.put("SO_TRUNG_BINH", trungBinh);
        params.put("SO_YEU_KEM", yeuKem);
        params.put("NGAY_XUAT", "Ngày " + now.getDayOfMonth() + " tháng " + now.getMonthValue() + " năm " + now.getYear());
        params.put("NGUOI_LAP", (nguoiLap != null && !nguoiLap.isBlank()) ? nguoiLap : "Ban Thư Ký Hội Đồng");

        try {
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rows);
            JasperReport jasperReport = getCompiledReport("reports/Bang_Tong_Hop_DRL.jrxml");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (JRException e) {
            log.error("Lỗi khi xuất PDF bảng tổng hợp ĐRL", e);
            throw new RuntimeException("Lỗi xuất PDF bảng tổng hợp ĐRL: " + e.getMessage(), e);
        }
    }
}
