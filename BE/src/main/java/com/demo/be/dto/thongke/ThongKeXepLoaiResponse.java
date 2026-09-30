package com.demo.be.dto.thongke;

public record ThongKeXepLoaiResponse(
        String xepLoai,
        long soLuong
) {
    /**
     * Constructor hỗ trợ @SqlResultSetMapping và @ConstructorResult của JPA,
     * tự động xử lý an toàn giá trị null từ SQL Server.
     */
    public ThongKeXepLoaiResponse(
            String xepLoai,
            Long soLuong
    ) {
        this(
                xepLoai != null ? xepLoai : "",
                soLuong != null ? soLuong : 0L
        );
    }
}
