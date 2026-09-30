package com.demo.be.config;

import com.demo.be.model.AppUser;
import com.demo.be.model.Role;
import com.demo.be.model.DotDanhGiaDrl;
import com.demo.be.repository.DotDanhGiaRepository;
import com.demo.be.repository.SinhVienRepository;
import com.demo.be.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final DateTimeFormatter DOB_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SinhVienRepository sinhVienRepository;
    private final DotDanhGiaRepository dotDanhGiaRepository;

    public DataInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            SinhVienRepository sinhVienRepository,
            DotDanhGiaRepository dotDanhGiaRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sinhVienRepository = sinhVienRepository;
        this.dotDanhGiaRepository = dotDanhGiaRepository;
    }

    @Override
    public void run(String... args) {
        initDefaultAdmin();
        initStudentAccounts();
        sanitizeDotDanhGia();
    }

    private void sanitizeDotDanhGia() {
        dotDanhGiaRepository.findAll().forEach(dot -> {
            if (dot.getHocKy() != null && dot.getTenDot() != null) {
                String expectedHk = "Học kỳ " + dot.getHocKy();
                if (dot.getTenDot().contains("Học kỳ") && !dot.getTenDot().contains(expectedHk)) {
                    String corrected = dot.getTenDot().replaceAll("Học kỳ\\s*\\d+", expectedHk);
                    log.info("Tự động đồng bộ tên đợt ID {}: '{}' -> '{}'", dot.getId(), dot.getTenDot(), corrected);
                    dot.setTenDot(corrected);
                    dotDanhGiaRepository.save(dot);
                }
            }
        });
    }

    private void initDefaultAdmin() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Quản trị hệ thống");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            log.info("Initialized default admin account: admin / admin123");
        }
    }

    private void initStudentAccounts() {
        sinhVienRepository.findAll().forEach(sv -> {
            if (userRepository.findByUsername(sv.getMssv()).isEmpty()) {
                AppUser user = new AppUser();
                user.setUsername(sv.getMssv());
                user.setFullName(sv.getHoTen());
                String defaultPassword = sv.getNgaySinh() != null
                        ? sv.getNgaySinh().format(DOB_FORMATTER)
                        : "student123";
                user.setPassword(passwordEncoder.encode(defaultPassword));
                user.setRole(Role.STUDENT);
                user.setEnabled(sv.isActive());
                userRepository.save(user);
                log.info("Initialized student account: {} / {}", sv.getMssv(), defaultPassword);
            }
        });
    }
}
