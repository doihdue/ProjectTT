package com.demo.be.service;

import com.demo.be.dto.diemrenluyen.DiemRenLuyenDuyetRequest;
import com.demo.be.dto.diemrenluyen.DiemRenLuyenResponse;
import com.demo.be.dto.diemrenluyen.DiemRenLuyenSubmitRequest;
import com.demo.be.dto.diemrenluyen.DrlThongKeResponse;
import java.util.List;

public interface DiemRenLuyenService {

    static String xepLoaiDrl(int diem) {
        if (diem >= 90) return "Xuất sắc";
        if (diem >= 80) return "Tốt";
        if (diem >= 65) return "Khá";
        if (diem >= 50) return "Trung bình";
        if (diem >= 35) return "Yếu";
        return "Kém";
    }

    DiemRenLuyenResponse submitDrl(String mssv, DiemRenLuyenSubmitRequest request);

    List<DiemRenLuyenResponse> getMyDrlList(String mssv);

    List<DiemRenLuyenResponse> getAllForAdmin(Long dotDanhGiaId, String trangThai, Integer hocKy, String namHoc, Long khoaId, Long lopId, String keyword);

    DiemRenLuyenResponse getById(Long id);

    DiemRenLuyenResponse duyetDrl(Long id, DiemRenLuyenDuyetRequest request, String adminUsername);

    void delete(Long id);

    DrlThongKeResponse getThongKe();
}
