package com.demo.be.service;

import com.demo.be.dto.lop.LopRequest;
import com.demo.be.dto.lop.LopResponse;
import java.util.List;

public interface LopService {

    List<LopResponse> findAll();

    LopResponse findById(Long id);

    String generateNextMaLop(Long khoaId, String nienKhoa);

    LopResponse create(LopRequest request);

    LopResponse update(Long id, LopRequest request);

    void delete(Long id);
}
