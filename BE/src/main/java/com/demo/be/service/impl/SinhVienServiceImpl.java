package com.demo.be.service.impl;

import com.demo.be.dto.sinhvien.SinhVienRequest;
import com.demo.be.dto.sinhvien.SinhVienResponse;
import com.demo.be.dto.sinhvien.StudentProfileUpdateRequest;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.AppUser;
import com.demo.be.model.Khoa;
import com.demo.be.model.Lop;
import com.demo.be.model.Role;
import com.demo.be.model.SinhVien;
import com.demo.be.repository.DiemRenLuyenRepository;
import com.demo.be.repository.LopRepository;
import com.demo.be.repository.SinhVienRepository;
import com.demo.be.repository.UserRepository;
import com.demo.be.service.SinhVienService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SinhVienServiceImpl implements SinhVienService {

    private static final DateTimeFormatter DOB_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final SinhVienRepository sinhVienRepository;
    private final LopRepository lopRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DiemRenLuyenRepository diemRenLuyenRepository;

    public SinhVienServiceImpl(
            SinhVienRepository sinhVienRepository,
            LopRepository lopRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            DiemRenLuyenRepository diemRenLuyenRepository
    ) {
        this.sinhVienRepository = sinhVienRepository;
        this.lopRepository = lopRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.diemRenLuyenRepository = diemRenLuyenRepository;
    }

    @Override
    public String generateNextMssv(Long lopId, LocalDate ngayNhapHoc) {
        Lop lop = lopRepository.findById(lopId)
                .orElseThrow(() -> new ResourceNotFoundException("Lop not found with id " + lopId));

        String year;
        if (ngayNhapHoc != null) {
            year = String.valueOf(ngayNhapHoc.getYear());
        } else if (lop.getNienKhoa() != null && !lop.getNienKhoa().isBlank()) {
            Matcher m = Pattern.compile("\\d{4}").matcher(lop.getNienKhoa());
            year = m.find() ? m.group() : String.valueOf(LocalDate.now().getYear());
        } else {
            year = String.valueOf(LocalDate.now().getYear());
        }

        Khoa khoa = lop.getKhoa();
        String maKhoa = (khoa != null && khoa.getMaKhoa() != null && !khoa.getMaKhoa().isBlank())
                ? khoa.getMaKhoa().trim().toUpperCase()
                : "CNTT";

        String prefix = year + maKhoa;

        List<String> existingMssvs = sinhVienRepository.findMssvByPrefix(prefix);
        int maxSeq = 0;
        for (String mssv : existingMssvs) {
            if (mssv != null && mssv.startsWith(prefix)) {
                String suffix = mssv.substring(prefix.length());
                if (suffix.matches("\\d+")) {
                    try {
                        int seq = Integer.parseInt(suffix);
                        if (seq > maxSeq) {
                            maxSeq = seq;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        int nextSeq = maxSeq + 1;
        return String.format("%s%03d", prefix, nextSeq);
    }

    @Override
    public List<SinhVienResponse> findAll() {
        return sinhVienRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public SinhVienResponse findById(Long id) {
        return toResponse(getSinhVien(id));
    }

    @Override
    public SinhVienResponse findByMssv(String mssv) {
        return toResponse(sinhVienRepository.findByMssv(mssv)
                .orElseThrow(() -> new ResourceNotFoundException("SinhVien not found with MSSV " + mssv)));
    }

    @Override
    @Transactional
    public SinhVienResponse updateProfile(String mssv, StudentProfileUpdateRequest request) {
        SinhVien sinhVien = sinhVienRepository.findByMssv(mssv)
                .orElseThrow(() -> new ResourceNotFoundException("SinhVien not found with MSSV " + mssv));
        if (request.email() != null && !request.email().isBlank()) {
            sinhVien.setEmail(request.email().trim());
        }
        sinhVien.setSoDienThoai(request.soDienThoai() != null ? request.soDienThoai().trim() : null);
        sinhVien.setDiaChi(request.diaChi() != null ? request.diaChi().trim() : null);
        return toResponse(sinhVienRepository.save(sinhVien));
    }

    @Override
    @Transactional
    public SinhVienResponse create(SinhVienRequest request) {
        SinhVien sinhVien = new SinhVien();
        apply(request, sinhVien);
        if (sinhVien.getMssv() == null || sinhVien.getMssv().isBlank()) {
            sinhVien.setMssv(generateNextMssv(request.lopId(), request.ngayNhapHoc()));
        }
        if (sinhVien.getEmail() == null || sinhVien.getEmail().isBlank()) {
            sinhVien.setEmail(SinhVienService.generateStudentEmail(sinhVien.getHoTen(), sinhVien.getMssv()));
        }
        SinhVien saved = sinhVienRepository.save(sinhVien);

        // Tạo tài khoản AppUser cho sinh viên để đăng nhập ngay (mật khẩu mặc định: ngày sinh ddMMyyyy hoặc student123)
        if (userRepository.findByUsername(saved.getMssv()).isEmpty()) {
            AppUser studentUser = new AppUser();
            studentUser.setUsername(saved.getMssv());
            studentUser.setFullName(saved.getHoTen());

            String defaultPassword = saved.getNgaySinh() != null
                    ? saved.getNgaySinh().format(DOB_FORMATTER)
                    : "student123";

            studentUser.setPassword(passwordEncoder.encode(defaultPassword));
            studentUser.setRole(Role.STUDENT);
            studentUser.setEnabled(saved.isActive());
            userRepository.save(studentUser);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public SinhVienResponse update(Long id, SinhVienRequest request) {
        SinhVien sinhVien = getSinhVien(id);
        String oldMssv = sinhVien.getMssv();
        apply(request, sinhVien);
        if (sinhVien.getEmail() == null || sinhVien.getEmail().isBlank()) {
            sinhVien.setEmail(SinhVienService.generateStudentEmail(sinhVien.getHoTen(), sinhVien.getMssv()));
        }
        SinhVien saved = sinhVienRepository.save(sinhVien);

        // Đồng bộ AppUser
        userRepository.findByUsername(oldMssv).ifPresentOrElse(
                u -> {
                    u.setUsername(saved.getMssv());
                    u.setFullName(saved.getHoTen());
                    u.setEnabled(saved.isActive());
                    userRepository.save(u);
                },
                () -> {
                    AppUser studentUser = new AppUser();
                    studentUser.setUsername(saved.getMssv());
                    studentUser.setFullName(saved.getHoTen());
                    String defaultPassword = saved.getNgaySinh() != null
                            ? saved.getNgaySinh().format(DOB_FORMATTER)
                            : "student123";
                    studentUser.setPassword(passwordEncoder.encode(defaultPassword));
                    studentUser.setRole(Role.STUDENT);
                    studentUser.setEnabled(saved.isActive());
                    userRepository.save(studentUser);
                }
        );

        return toResponse(saved);
    }

    @Override
    @Transactional
    public String resetPassword(Long id) {
        SinhVien sinhVien = getSinhVien(id);
        String defaultPassword = sinhVien.getNgaySinh() != null
                ? sinhVien.getNgaySinh().format(DOB_FORMATTER)
                : "student123";

        AppUser user = userRepository.findByUsername(sinhVien.getMssv()).orElseGet(() -> {
            AppUser u = new AppUser();
            u.setUsername(sinhVien.getMssv());
            u.setFullName(sinhVien.getHoTen());
            u.setRole(Role.STUDENT);
            u.setEnabled(sinhVien.isActive());
            return u;
        });

        user.setPassword(passwordEncoder.encode(defaultPassword));
        userRepository.save(user);
        return defaultPassword;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SinhVien sinhVien = getSinhVien(id);
        String mssv = sinhVien.getMssv();

        // Xóa các điểm rèn luyện liên quan
        diemRenLuyenRepository.deleteBySinhVien_Id(id);

        // Xóa AppUser nếu có
        userRepository.findByUsername(mssv).ifPresent(userRepository::delete);

        sinhVienRepository.delete(sinhVien);
    }

    private SinhVien getSinhVien(Long id) {
        return sinhVienRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SinhVien not found with id " + id));
    }

    private void apply(SinhVienRequest request, SinhVien sinhVien) {
        Lop lop = lopRepository.findById(request.lopId())
                .orElseThrow(() -> new ResourceNotFoundException("Lop not found with id " + request.lopId()));
        if (request.mssv() != null && !request.mssv().isBlank()) {
            sinhVien.setMssv(request.mssv().trim());
        }
        sinhVien.setHoTen(request.hoTen());
        sinhVien.setNgaySinh(request.ngaySinh());
        sinhVien.setGioiTinh(request.gioiTinh());
        if (request.email() != null && !request.email().isBlank()) {
            sinhVien.setEmail(request.email().trim());
        }
        sinhVien.setSoDienThoai(request.soDienThoai());
        sinhVien.setDiaChi(request.diaChi());
        sinhVien.setNgayNhapHoc(request.ngayNhapHoc());
        sinhVien.setLop(lop);
        sinhVien.setActive(request.active() == null || request.active());
    }

    private SinhVienResponse toResponse(SinhVien sinhVien) {
        Lop lop = sinhVien.getLop();
        Khoa khoa = lop != null ? lop.getKhoa() : null;
        String khoaHoc = SinhVienService.extractKhoaHocFromMssv(sinhVien.getMssv());
        return new SinhVienResponse(
                sinhVien.getId(),
                sinhVien.getMssv(),
                sinhVien.getHoTen(),
                sinhVien.getNgaySinh(),
                sinhVien.getGioiTinh(),
                sinhVien.getEmail(),
                sinhVien.getSoDienThoai(),
                sinhVien.getDiaChi(),
                sinhVien.getNgayNhapHoc(),
                lop != null ? lop.getId() : null,
                lop != null ? lop.getTenLop() : null,
                khoa != null ? khoa.getId() : null,
                khoa != null ? khoa.getTenKhoa() : null,
                khoaHoc,
                sinhVien.isActive()
        );
    }
}
