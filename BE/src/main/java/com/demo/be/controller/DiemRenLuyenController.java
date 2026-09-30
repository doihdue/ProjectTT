package com.demo.be.controller;

import com.demo.be.dto.diemrenluyen.DiemRenLuyenDuyetRequest;
import com.demo.be.dto.diemrenluyen.DiemRenLuyenResponse;
import com.demo.be.dto.diemrenluyen.DiemRenLuyenSubmitRequest;
import com.demo.be.dto.diemrenluyen.DrlThongKeResponse;
import com.demo.be.service.DiemRenLuyenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

import com.demo.be.service.JasperReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/diem-ren-luyen")
@RequiredArgsConstructor
public class DiemRenLuyenController {

    private final DiemRenLuyenService diemRenLuyenService;
    private final JasperReportService jasperReportService;

    @PostMapping("/submit")
    public ResponseEntity<DiemRenLuyenResponse> submitDrl(
            Principal principal,
            @Valid @RequestBody DiemRenLuyenSubmitRequest request
    ) {
        String mssv = principal.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(diemRenLuyenService.submitDrl(mssv, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<DiemRenLuyenResponse>> getMyDrlList(Principal principal) {
        String mssv = principal.getName();
        return ResponseEntity.ok(diemRenLuyenService.getMyDrlList(mssv));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<DiemRenLuyenResponse>> getAllForAdmin(
            @RequestParam(required = false) Long dotDanhGiaId,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) Integer hocKy,
            @RequestParam(required = false) String namHoc,
            @RequestParam(required = false) Long khoaId,
            @RequestParam(required = false) Long lopId,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(diemRenLuyenService.getAllForAdmin(dotDanhGiaId, trangThai, hocKy, namHoc, khoaId, lopId, keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiemRenLuyenResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(diemRenLuyenService.getById(id));
    }

    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<byte[]> exportPhieuPdf(@PathVariable Long id, Principal principal) {
        DiemRenLuyenResponse drl = diemRenLuyenService.getById(id);
        byte[] pdfBytes = jasperReportService.exportPhieuDrlPdf(id);

        String filename = "Phieu_DRL_" + (drl.mssv() != null ? drl.mssv() : id) + "_HK" + (drl.hocKy() != null ? drl.hocKy() : "") + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }

    @GetMapping("/export-summary-pdf")
    public ResponseEntity<byte[]> exportSummaryPdf(
            @RequestParam(required = false) Long dotDanhGiaId,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) Integer hocKy,
            @RequestParam(required = false) String namHoc,
            @RequestParam(required = false) Long khoaId,
            @RequestParam(required = false) Long lopId,
            @RequestParam(required = false) String keyword,
            Principal principal
    ) {
        String nguoiLap = principal != null ? principal.getName() : "Admin";
        byte[] pdfBytes = jasperReportService.exportBangTongHopDrlPdf(
                dotDanhGiaId, trangThai, hocKy, namHoc, khoaId, lopId, keyword, nguoiLap
        );

        String filename = "Bang_Tong_Hop_DRL_" + (namHoc != null ? namHoc.replace('/', '-') : "All") + ".pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }

    @PutMapping("/{id}/duyet")
    public ResponseEntity<DiemRenLuyenResponse> duyetDrl(
            @PathVariable Long id,
            Principal principal,
            @Valid @RequestBody DiemRenLuyenDuyetRequest request
    ) {
        String adminUsername = principal != null ? principal.getName() : "admin";
        return ResponseEntity.ok(diemRenLuyenService.duyetDrl(id, request, adminUsername));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        diemRenLuyenService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/thong-ke")
    public ResponseEntity<DrlThongKeResponse> getThongKe() {
        return ResponseEntity.ok(diemRenLuyenService.getThongKe());
    }
}
