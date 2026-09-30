package com.demo.be.service;

public interface JasperReportService {

    byte[] exportPhieuDrlPdf(Long drlId);

    byte[] exportBangTongHopDrlPdf(
            Long dotDanhGiaId,
            String trangThai,
            Integer hocKy,
            String namHoc,
            Long khoaId,
            Long lopId,
            String keyword,
            String nguoiLap
    );
}
