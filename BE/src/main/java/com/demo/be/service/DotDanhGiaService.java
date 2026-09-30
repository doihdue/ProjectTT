package com.demo.be.service;

import com.demo.be.dto.diemrenluyen.DotDanhGiaRequest;
import com.demo.be.dto.diemrenluyen.DotDanhGiaResponse;
import com.demo.be.model.DotDanhGiaDrl;
import java.util.List;

public interface DotDanhGiaService {

    List<DotDanhGiaResponse> getAllDots();

    List<DotDanhGiaResponse> getOpenDots();

    DotDanhGiaResponse getById(Long id);

    DotDanhGiaResponse createDot(DotDanhGiaRequest request);

    DotDanhGiaResponse updateDot(Long id, DotDanhGiaRequest request);

    DotDanhGiaResponse toggleTrangThai(Long id);

    void deleteDot(Long id);

    DotDanhGiaDrl getDot(Long id);
}
