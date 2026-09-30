package com.demo.be.service;

import com.demo.be.dto.report.DrlReportRowDto;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JasperReportCompileTest {

    @Test
    void testCompileAndFillPhieuDrl() throws Exception {
        ClassPathResource resource = new ClassPathResource("reports/Phieu_Diem_Ren_Luyen.jrxml");
        JasperReport jasperReport;
        try (InputStream is = resource.getInputStream()) {
            jasperReport = JasperCompileManager.compileReport(is);
        }
        assertNotNull(jasperReport);

        Map<String, Object> params = new HashMap<>();
        params.put("TEN_TRUONG", "TRƯỜNG ĐẠI HỌC CÔNG NGHỆ");
        params.put("TEN_KHOA", "Công Nghệ Thông Tin");
        params.put("TEN_LOP", "CNTT2023A");
        params.put("MSSV", "SV20230001");
        params.put("HO_TEN", "Nguyễn Văn An");
        params.put("NGAY_SINH", "15/08/2004");
        params.put("GIOI_TINH", "Nam");
        params.put("TEN_DOT", "Đợt Đánh Giá HK1 (2024-2025)");
        params.put("HOC_KY", "1");
        params.put("NAM_HOC", "2024-2025");
        params.put("TC1_SV", 18);
        params.put("TC2_SV", 23);
        params.put("TC3_SV", 19);
        params.put("TC4_SV", 22);
        params.put("TC5_SV", 8);
        params.put("TONG_SV", 90);
        params.put("TC1_ADMIN", 18);
        params.put("TC2_ADMIN", 23);
        params.put("TC3_ADMIN", 19);
        params.put("TC4_ADMIN", 22);
        params.put("TC5_ADMIN", 8);
        params.put("TONG_ADMIN", 90);
        params.put("XEP_LOAI", "Xuất sắc");
        params.put("TRANG_THAI", "DA_DUYET");
        params.put("NGAY_NOP", "20/09/2026 10:30");
        params.put("NGAY_DUYET", "22/09/2026 14:15");
        params.put("NGUOI_DUYET", "Admin QLSV");
        params.put("NHAN_XET_ADMIN", "Sinh viên chấp hành tốt nội quy và tích cực trong các phong trào Đoàn - Hội.");
        params.put("NGAY_XUAT", "Ngày 27 tháng 09 năm 2026");

        JasperPrint print = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
        byte[] pdf = JasperExportManager.exportReportToPdf(print);

        assertNotNull(pdf);
        assertTrue(pdf.length > 5000);
    }

    @Test
    void testCompileAndFillBangTongHop() throws Exception {
        ClassPathResource resource = new ClassPathResource("reports/Bang_Tong_Hop_DRL.jrxml");
        JasperReport jasperReport;
        try (InputStream is = resource.getInputStream()) {
            jasperReport = JasperCompileManager.compileReport(is);
        }
        assertNotNull(jasperReport);

        Map<String, Object> params = new HashMap<>();
        params.put("TEN_TRUONG", "TRƯỜNG ĐẠI HỌC CÔNG NGHỆ");
        params.put("TIEU_DE", "BẢNG TỔNG HỢP KẾT QUẢ ĐIỂM RÈN LUYỆN");
        params.put("TEN_DOT", "Đợt 1 HK1 2024-2025");
        params.put("TEN_KHOA", "Công Nghệ Thông Tin");
        params.put("TEN_LOP", "CNTT2023A");
        params.put("HOC_KY", "1");
        params.put("NAM_HOC", "2024-2025");
        params.put("TONG_SO_SV", 2);
        params.put("SO_XUAT_SAC", 1);
        params.put("SO_TOT", 1);
        params.put("SO_KHA", 0);
        params.put("SO_TRUNG_BINH", 0);
        params.put("SO_YEU_KEM", 0);
        params.put("NGAY_XUAT", "Ngày 27 tháng 09 năm 2026");
        params.put("NGUOI_LAP", "Admin");

        List<DrlReportRowDto> rows = List.of(
                DrlReportRowDto.builder()
                        .stt(1)
                        .mssv("SV20230001")
                        .hoTen("Nguyễn Văn An")
                        .tenLop("CNTT2023A")
                        .tenKhoa("Công Nghệ Thông Tin")
                        .tieuChi1Admin(18)
                        .tieuChi2Admin(23)
                        .tieuChi3Admin(19)
                        .tieuChi4Admin(22)
                        .tieuChi5Admin(8)
                        .tongDiemSv(90)
                        .tongDiemAdmin(90)
                        .xepLoai("Xuất sắc")
                        .trangThai("DA_DUYET")
                        .build(),
                DrlReportRowDto.builder()
                        .stt(2)
                        .mssv("SV20230002")
                        .hoTen("Trần Thị Bình")
                        .tenLop("CNTT2023A")
                        .tenKhoa("Công Nghệ Thông Tin")
                        .tieuChi1Admin(16)
                        .tieuChi2Admin(22)
                        .tieuChi3Admin(18)
                        .tieuChi4Admin(20)
                        .tieuChi5Admin(7)
                        .tongDiemSv(83)
                        .tongDiemAdmin(83)
                        .xepLoai("Tốt")
                        .trangThai("DA_DUYET")
                        .build()
        );

        JasperPrint print = JasperFillManager.fillReport(jasperReport, params, new JRBeanCollectionDataSource(rows));
        byte[] pdf = JasperExportManager.exportReportToPdf(print);

        assertNotNull(pdf);
        assertTrue(pdf.length > 5000);
    }
}
