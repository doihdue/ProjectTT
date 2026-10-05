package com.demo.be.exception;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult().getAllErrors().stream()
                .map(err -> {
                    if (err instanceof FieldError fe) {
                        return fe.getField() + ": " + fe.getDefaultMessage();
                    }
                    String defaultMsg = err.getDefaultMessage() != null ? err.getDefaultMessage() : "";
                    String lower = defaultMsg.toLowerCase();
                    if (lower.contains("năm bắt đầu") || lower.contains("niên khóa") || lower.contains("nienkhoa")) {
                        return "nienKhoa: " + defaultMsg;
                    }
                    if (lower.contains("năm học")) {
                        return "namHoc: " + defaultMsg;
                    }
                    if (lower.contains("ngày kết thúc")) {
                        return "ngayKetThuc: " + defaultMsg;
                    }
                    return defaultMsg;
                })
                .collect(Collectors.toList());

        String summary = exception.getBindingResult().getAllErrors().stream()
                .map(org.springframework.validation.ObjectError::getDefaultMessage)
                .filter(msg -> msg != null && !msg.isBlank())
                .distinct()
                .collect(Collectors.joining("; "));

        if (summary.isBlank()) {
            summary = "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại các trường thông tin.";
        }

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                summary,
                details
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        String msg = exception.getMessage() != null ? exception.getMessage() : "Yêu cầu không hợp lệ";
        List<String> details = new java.util.ArrayList<>();
        String lower = msg.toLowerCase();
        if (lower.contains("mã khoa") || lower.contains("makhoa")) {
            details.add("maKhoa: " + msg);
        } else if (lower.contains("tên khoa") || lower.contains("tenkhoa")) {
            details.add("tenKhoa: " + msg);
        } else if (lower.contains("mã lớp") || lower.contains("malop")) {
            details.add("maLop: " + msg);
        } else if (lower.contains("tên lớp") || lower.contains("tenlop")) {
            details.add("tenLop: " + msg);
        } else if (lower.contains("niên khóa") || lower.contains("nienkhoa") || lower.contains("năm bắt đầu")) {
            details.add("nienKhoa: " + msg);
        } else if (lower.contains("mssv")) {
            details.add("mssv: " + msg);
        } else if (lower.contains("email")) {
            details.add("email: " + msg);
        } else {
            details.add(msg);
        }

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                msg,
                details
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException exception) {
        String rootMsg = exception.getRootCause() != null ? exception.getRootCause().getMessage() : exception.getMessage();
        if (rootMsg == null) rootMsg = "";
        String lower = rootMsg.toLowerCase();

        String message = "Dữ liệu bị trùng lặp hoặc vi phạm ràng buộc dữ liệu.";
        List<String> details = new java.util.ArrayList<>();

        if (lower.contains("ma_khoa") || lower.contains("makhoa") || lower.contains("dbo.khoa") || lower.contains("'khoa'") || lower.contains("khoa")) {
            message = "Mã khoa này đã tồn tại trong hệ thống. Vui lòng chọn mã khoa khác.";
            details.add("maKhoa: Mã khoa đã tồn tại");
        } else if (lower.contains("ma_lop") || lower.contains("malop") || lower.contains("dbo.lop") || lower.contains("'lop'") || lower.contains("lop")) {
            message = "Mã lớp này đã tồn tại trong hệ thống. Vui lòng chọn mã lớp khác.";
            details.add("maLop: Mã lớp đã tồn tại");
        } else if (lower.contains("mssv") || lower.contains("dbo.sinh_vien") || lower.contains("sinh_vien") || lower.contains("sinhvien")) {
            message = "Mã số sinh viên (MSSV) này đã tồn tại trong hệ thống.";
            details.add("mssv: MSSV đã tồn tại");
        } else if (lower.contains("email")) {
            message = "Địa chỉ email này đã được sử dụng. Vui lòng nhập email khác.";
            details.add("email: Email đã tồn tại");
        } else if (lower.contains("ten_khoa") || lower.contains("tenkhoa")) {
            message = "Tên khoa này đã tồn tại trong hệ thống. Vui lòng chọn tên khoa khác.";
            details.add("tenKhoa: Tên khoa đã tồn tại");
        } else if (lower.contains("foreign key") || lower.contains("foreign_key") || lower.contains("cannot delete or update a parent row")) {
            message = "Không thể thao tác do bản ghi đang được liên kết với dữ liệu khác.";
            details.add("database: Vi phạm ràng buộc khóa ngoại (Foreign Key)");
        } else {
            message = "Dữ liệu bị trùng lặp hoặc vi phạm ràng buộc cơ sở dữ liệu.";
            details.add(message);
        }

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                message,
                details
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(RuntimeException exception) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                exception.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                exception.getMessage() != null ? exception.getMessage() : "Lỗi hệ thống máy chủ",
                List.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
