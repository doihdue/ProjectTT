package com.demo.be.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrlReportRowDto {
    private Integer stt;
    private String mssv;
    private String hoTen;
    private String tenLop;
    private String tenKhoa;
    private Integer tieuChi1Admin;
    private Integer tieuChi2Admin;
    private Integer tieuChi3Admin;
    private Integer tieuChi4Admin;
    private Integer tieuChi5Admin;
    private Integer tongDiemSv;
    private Integer tongDiemAdmin;
    private String xepLoai;
    private String trangThai;
}
