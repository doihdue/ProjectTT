package com.demo.be.service;

import com.demo.be.dto.common.PageResponse;
import com.demo.be.dto.sinhvien.SinhVienRequest;
import com.demo.be.dto.sinhvien.SinhVienResponse;
import com.demo.be.dto.sinhvien.StudentProfileUpdateRequest;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface SinhVienService {

    Pattern MSSV_KHOA_PATTERN = Pattern.compile("^[A-Za-z]+(\\d{2})");

    static String extractKhoaHocFromMssv(String mssv) {
        if (mssv == null || mssv.isBlank()) return "";
        String trimmed = mssv.trim();
        Matcher mYear = Pattern.compile("^(\\d{4})").matcher(trimmed);
        if (mYear.find()) {
            return mYear.group(1);
        }
        Matcher m = MSSV_KHOA_PATTERN.matcher(trimmed);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    static String removeAccents(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String withoutAccents = pattern.matcher(normalized).replaceAll("");
        return withoutAccents.replace('đ', 'd').replace('Đ', 'D');
    }

    static String generateEmailPrefixFromName(String hoTen) {
        if (hoTen == null || hoTen.isBlank()) return "";
        String clean = removeAccents(hoTen.trim()).toLowerCase();
        String[] words = clean.split("\\s+");
        if (words.length == 0) return "";
        if (words.length == 1) return words[0];

        String lastName = words[words.length - 1];
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < words.length - 1; i++) {
            if (!words[i].isEmpty()) {
                initials.append(words[i].charAt(0));
            }
        }
        return lastName + initials.toString();
    }

    static String extractMssvSuffixForEmail(String mssv) {
        if (mssv == null || mssv.isBlank()) return "";
        String trimmed = mssv.trim();
        if (trimmed.matches("^\\d{4}.+")) {
            return trimmed.substring(4);
        }
        return trimmed;
    }

    static String generateStudentEmail(String hoTen, String mssv) {
        String namePart = generateEmailPrefixFromName(hoTen);
        String mssvPart = extractMssvSuffixForEmail(mssv);
        if (namePart.isEmpty() && mssvPart.isEmpty()) {
            return "student@.stu.edu.vn";
        }
        if (namePart.isEmpty()) {
            return mssvPart + "@.stu.edu.vn";
        }
        if (mssvPart.isEmpty()) {
            return namePart + "@.stu.edu.vn";
        }
        return namePart + "." + mssvPart + "@.stu.edu.vn";
    }

    String generateNextMssv(Long lopId, LocalDate ngayNhapHoc);

    List<SinhVienResponse> findAll();

    PageResponse<SinhVienResponse> searchAndFilter(String keyword, Long lopId, String gioiTinh, int page, int size);

    SinhVienResponse findById(Long id);

    SinhVienResponse findByMssv(String mssv);

    SinhVienResponse updateProfile(String mssv, StudentProfileUpdateRequest request);

    SinhVienResponse create(SinhVienRequest request);

    SinhVienResponse update(Long id, SinhVienRequest request);

    String resetPassword(Long id);

    void delete(Long id);
}
