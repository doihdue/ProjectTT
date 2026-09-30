package com.demo.be.dto.thongke;

public record ThongKeKhoaResponse(
        Long khoaId,
        String maKhoa,
        String tenKhoa,
        long tongSinhVien,
        long soPhieuDaNop,
        long soPhieuDaDuyet,
        double diemTrungBinh,
        int diemCaoNhat
) {
    /**
     * Constructor hỗ trợ @SqlResultSetMapping và @ConstructorResult của JPA,
     * tự động xử lý an toàn giá trị null từ SQL Server.
     */
    public ThongKeKhoaResponse(
            Long khoaId,
            String maKhoa,
            String tenKhoa,
            Long tongSinhVien,
            Long soPhieuDaNop,
            Long soPhieuDaDuyet,
            Double diemTrungBinh,
            Integer diemCaoNhat
    ) {
        this(
                khoaId,
                maKhoa,
                tenKhoa,
                tongSinhVien != null ? tongSinhVien : 0L,
                soPhieuDaNop != null ? soPhieuDaNop : 0L,
                soPhieuDaDuyet != null ? soPhieuDaDuyet : 0L,
                diemTrungBinh != null ? diemTrungBinh : 0.0,
                diemCaoNhat != null ? diemCaoNhat : 0
        );
    }
}
