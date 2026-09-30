package com.demo.be.service;

import com.demo.be.model.Khoa;
import com.demo.be.model.Lop;
import com.demo.be.repository.KhoaRepository;
import com.demo.be.repository.LopRepository;
import com.demo.be.repository.SinhVienRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.demo.be.repository.UserRepository;
import com.demo.be.repository.DiemRenLuyenRepository;
import com.demo.be.service.impl.LopServiceImpl;
import com.demo.be.service.impl.SinhVienServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AutoGenerationTest {

    @Mock
    private LopRepository lopRepository;

    @Mock
    private KhoaRepository khoaRepository;

    @Mock
    private SinhVienRepository sinhVienRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DiemRenLuyenRepository diemRenLuyenRepository;

    @InjectMocks
    private LopServiceImpl lopService;

    @InjectMocks
    private SinhVienServiceImpl sinhVienService;

    @Test
    void testGenerateNextMaLop_FirstClass() {
        Khoa khoa = new Khoa();
        khoa.setId(1L);
        khoa.setMaKhoa("CNTT");
        when(khoaRepository.findById(1L)).thenReturn(Optional.of(khoa));
        when(lopRepository.findMaLopByPrefix("D2022CNTT")).thenReturn(List.of());

        String maLop = lopService.generateNextMaLop(1L, "2022-2026");
        assertEquals("D2022CNTT01", maLop);
    }

    @Test
    void testGenerateNextMaLop_SubsequentClass() {
        Khoa khoa = new Khoa();
        khoa.setId(1L);
        khoa.setMaKhoa("CNTT");
        when(khoaRepository.findById(1L)).thenReturn(Optional.of(khoa));
        when(lopRepository.findMaLopByPrefix("D2022CNTT")).thenReturn(List.of("D2022CNTT01", "D2022CNTT02"));

        String maLop = lopService.generateNextMaLop(1L, "2022-2026");
        assertEquals("D2022CNTT03", maLop);
    }

    @Test
    void testGenerateNextMssv_FirstStudent() {
        Khoa khoa = new Khoa();
        khoa.setId(1L);
        khoa.setMaKhoa("CNTT");

        Lop lop = new Lop();
        lop.setId(10L);
        lop.setKhoa(khoa);
        lop.setNienKhoa("2022-2026");

        when(lopRepository.findById(10L)).thenReturn(Optional.of(lop));
        when(sinhVienRepository.findMssvByPrefix("2022CNTT")).thenReturn(List.of());

        String mssv = sinhVienService.generateNextMssv(10L, LocalDate.of(2022, 9, 5));
        assertEquals("2022CNTT001", mssv);
    }

    @Test
    void testGenerateNextMssv_SubsequentStudent() {
        Khoa khoa = new Khoa();
        khoa.setId(1L);
        khoa.setMaKhoa("CNTT");

        Lop lop = new Lop();
        lop.setId(10L);
        lop.setKhoa(khoa);
        lop.setNienKhoa("2022-2026");

        when(lopRepository.findById(10L)).thenReturn(Optional.of(lop));
        when(sinhVienRepository.findMssvByPrefix("2022CNTT"))
                .thenReturn(List.of("2022CNTT001", "2022CNTT002"));

        String mssv = sinhVienService.generateNextMssv(10L, LocalDate.of(2022, 9, 5));
        assertEquals("2022CNTT003", mssv);
    }

    @Test
    void testGenerateStudentEmail() {
        // Test case from user: Dỗ Minh Duệ / 2022CNTT001 -> duedm.CNTT001@.stu.edu.vn
        String email1 = SinhVienService.generateStudentEmail("Dỗ Minh Duệ", "2022CNTT001");
        assertEquals("duedm.CNTT001@.stu.edu.vn", email1);

        String email2 = SinhVienService.generateStudentEmail("Đỗ Minh Duệ", "2022CNTT001");
        assertEquals("duedm.CNTT001@.stu.edu.vn", email2);

        String email3 = SinhVienService.generateStudentEmail("Nguyễn Văn An", "2022CNTT002");
        assertEquals("annv.CNTT002@.stu.edu.vn", email3);
    }
}
