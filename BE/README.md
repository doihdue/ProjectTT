# HƯỚNG DẪN TEST TOÀN BỘ API TRÊN POSTMAN
> Dành cho việc copy-paste trực tiếp vào Postman (Không cần import file, không cần cài đặt biến môi trường).

---

## 📌 QUY TRÌNH TEST NHANH TRÊN POSTMAN

1. **Bước 1 (Đăng nhập lấy Token):** 
   - Gửi request `POST http://localhost:8080/api/auth/login` với tài khoản Admin hoặc Sinh viên.
   - Copy chuỗi `accessToken` trong kết quả trả về.
2. **Bước 2 (Gắn Token vào các request cần xác thực):**
   - Trong Postman, chuyển sang tab **Headers**, thêm header:
     - **Key:** `Authorization`
     - **Value:** `Bearer <dán_chuỗi_token_vào_đây>`
   - Hoặc chuyển sang tab **Auth** -> chọn Type **Bearer Token** -> dán token vào ô Token.
3. **Bước 3 (Gửi dữ liệu Body nếu có):**
   - Với các request `POST` / `PUT` / `PATCH`, vào tab **Body** -> chọn **raw** -> chọn kiểu **JSON** rồi copy đoạn JSON mẫu dán vào.

### 🔑 Tài khoản có sẵn trong hệ thống:
- **Tài khoản Admin:**
  - `username`: `admin`
  - `password`: `admin123`
- **Tài khoản Sinh viên:**
  - `username`: `<MSSV>` (ví dụ: `20240001` hoặc MSSV có trong database)
  - `password`: Ngày sinh viết liền `ddMMyyyy` (ví dụ: `15052002`), hoặc `student123` nếu không có ngày sinh.

---

## 📋 MỤC LỤC CÁC NHÓM API (37 ENDPOINTS)

1. [Nhóm 1: Xác thực (Auth) - 2 API](#1-nhóm-xác-thực-auth)
2. [Nhóm 2: Thống kê (Dashboard & Khoa) - 2 API](#2-nhóm-thống-kê-thong-ke)
3. [Nhóm 3: Thông báo (Notifications) - 4 API](#3-nhóm-thông-báo-thong-bao)
4. [Nhóm 4: Quản lý Khoa - 5 API](#4-nhóm-quản-lý-khoa-khoa)
5. [Nhóm 5: Quản lý Lớp học - 6 API](#5-nhóm-quản-lý-lớp-học-lop)
6. [Nhóm 6: Quản lý Sinh viên - 9 API](#6-nhóm-quản-lý-sinh-viên-sinh-vien)
7. [Nhóm 7: Quản lý Đợt đánh giá ĐRL - 7 API](#7-nhóm-đợt-đánh-giá-đrl-dot-danh-gia)
8. [Nhóm 8: Điểm rèn luyện & Báo cáo PDF - 10 API](#8-nhóm-điểm-rèn-luyện--báo-cáo-pdf-diem-ren-luyen)

---

## CHI TIẾT TỪNG API

### 1. NHÓM XÁC THỰC (AUTH)

#### 1.1. Đăng nhập (Login)
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/auth/login`
- **Auth:** Không cần (Public)
- **Headers:** `Content-Type: application/json`
- **Body (raw JSON):**
  ```json
  {
    "username": "admin",
    "password": "admin123"
  }
  ```
- **Kết quả trả về (200 OK):**
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "username": "admin",
    "fullName": "Quản trị hệ thống",
    "role": "ADMIN"
  }
  ```
  *(Copy chuỗi `accessToken` để dùng cho các request bên dưới)*

#### 1.2. Đổi mật khẩu
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/auth/change-password`
- **Headers:**
  - `Authorization`: `Bearer <token>`
  - `Content-Type`: `application/json`
- **Body (raw JSON):**
  ```json
  {
    "oldPassword": "admin123",
    "newPassword": "admin456_new"
  }
  ```
- **Kết quả trả về:** `204 No Content`

---

### 2. NHÓM THỐNG KÊ (THONG-KE)

#### 2.1. Thống kê tổng quan Dashboard
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/thong-ke/dashboard`
- **Auth:** Không cần (Public)
- **Kết quả trả về (200 OK):** Trả về tổng sinh viên, tổng khoa, tổng lớp, số phiếu ĐRL chờ duyệt, đã duyệt và phân bố xếp loại.

#### 2.2. Thống kê điểm rèn luyện theo Khoa
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/thong-ke/khoa`
- **Auth:** Không cần (Public)
- **Kết quả trả về (200 OK):** Danh sách thống kê từng khoa gồm số SV, số phiếu nộp, đã duyệt, ĐTB, điểm cao nhất.

---

### 3. NHÓM THÔNG BÁO (THONG-BAO)

#### 3.1. Lấy danh sách thông báo của tài khoản hiện tại
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/thong-bao`
- **Headers:** `Authorization`: `Bearer <token>`

#### 3.2. Đếm số lượng thông báo chưa đọc
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/thong-bao/unread-count`
- **Headers:** `Authorization`: `Bearer <token>`
- **Kết quả mẫu:** `{"unreadCount": 3}`

#### 3.3. Đánh dấu 1 thông báo là đã đọc
- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/thong-bao/1/read` *(thay 1 bằng ID thông báo)*
- **Headers:** `Authorization`: `Bearer <token>`
- **Kết quả trả về:** `204 No Content`

#### 3.4. Đánh dấu tất cả thông báo là đã đọc
- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/thong-bao/read-all`
- **Headers:** `Authorization`: `Bearer <token>`
- **Kết quả trả về:** `204 No Content`

---

### 4. NHÓM QUẢN LÝ KHOA (KHOA)

#### 4.1. Lấy danh sách toàn bộ Khoa
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/khoa`
- **Headers:** `Authorization`: `Bearer <token_admin>`

#### 4.2. Lấy chi tiết Khoa theo ID
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/khoa/1`
- **Headers:** `Authorization`: `Bearer <token_admin>`

#### 4.3. Thêm mới Khoa
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/khoa`
- **Headers:**
  - `Authorization`: `Bearer <token_admin>`
  - `Content-Type`: `application/json`
- **Body (raw JSON):**
  ```json
  {
    "maKhoa": "CNTT",
    "tenKhoa": "Công Nghệ Thông Tin",
    "moTa": "Khoa Công nghệ thông tin và Truyền thông",
    "active": true
  }
  ```
- **Kết quả trả về:** `201 Created`

#### 4.4. Cập nhật thông tin Khoa
- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/khoa/1` *(thay 1 bằng ID khoa)*
- **Headers:**
  - `Authorization`: `Bearer <token_admin>`
  - `Content-Type`: `application/json`
- **Body (raw JSON):**
  ```json
  {
    "maKhoa": "CNTT",
    "tenKhoa": "Khoa CNTT (Nâng cao)",
    "moTa": "Đào tạo kỹ sư phần mềm",
    "active": true
  }
  ```
- **Kết quả trả về:** `200 OK`

#### 4.5. Xóa Khoa
- **Method:** `DELETE`
- **URL:** `http://localhost:8080/api/khoa/1` *(thay 1 bằng ID khoa)*
- **Headers:** `Authorization`: `Bearer <token_admin>`
- **Kết quả trả về:** `204 No Content`

---

### 5. NHÓM QUẢN LÝ LỚP HỌC (LOP)

#### 5.1. Lấy danh sách tất cả các Lớp
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/lop`
- **Headers:** `Authorization`: `Bearer <token_admin>`

#### 5.2. Tự động sinh mã Lớp tiếp theo
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/lop/next-ma-lop?khoaId=1&nienKhoa=2024-2028`
- **Headers:** `Authorization`: `Bearer <token_admin>`
- **Kết quả mẫu:** `{"maLop": "CNTT01"}`

#### 5.3. Xem chi tiết Lớp theo ID
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/lop/1`
- **Headers:** `Authorization`: `Bearer <token_admin>`

#### 5.4. Thêm mới Lớp học
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/lop`
- **Headers:**
  - `Authorization`: `Bearer <token_admin>`
  - `Content-Type`: `application/json`
- **Body (raw JSON):**
  ```json
  {
    "maLop": "CNTT01",
    "tenLop": "Công Nghệ Thông Tin 1",
    "nienKhoa": "2024-2028",
    "siSoToiDa": 50,
    "khoaId": 1,
    "active": true
  }
  ```
- **Kết quả trả về:** `201 Created`

#### 5.5. Cập nhật Lớp học
- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/lop/1` *(thay 1 bằng ID lớp)*
- **Headers:**
  - `Authorization`: `Bearer <token_admin>`
  - `Content-Type`: `application/json`
- **Body (raw JSON):** Tương tự body thêm mới lớp.

#### 5.6. Xóa Lớp học
- **Method:** `DELETE`
- **URL:** `http://localhost:8080/api/lop/1`
- **Headers:** `Authorization`: `Bearer <token_admin>`
- **Kết quả trả về:** `204 No Content`

---

### 6. NHÓM QUẢN LÝ SINH VIÊN (SINH-VIEN)

#### 6.1. Lấy danh sách toàn bộ Sinh viên
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/sinh-vien`
- **Headers:** `Authorization`: `Bearer <token_admin>`

#### 6.2. Tự động sinh MSSV kế tiếp
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/sinh-vien/next-mssv?lopId=1&ngayNhapHoc=2024-09-05`
- **Headers:** `Authorization`: `Bearer <token_admin>`
- **Kết quả mẫu:** `{"mssv": "20240001"}`

#### 6.3. Chi tiết Sinh viên theo ID
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/sinh-vien/1`
- **Headers:** `Authorization`: `Bearer <token_admin>`

#### 6.3b. [ENTITY MANAGER CƠ BẢN] Tìm Sinh viên bằng entityManager.find(...)
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/sinh-vien/em/1`
- **Ý nghĩa:** Minh họa dùng trực tiếp `entityManager.find(SinhVien.class, id)`.
- **Headers:** `Authorization`: `Bearer <token_admin>`

#### 6.4. Sinh viên xem thông tin hồ sơ của chính mình
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/sinh-vien/me`
- **Headers:** `Authorization`: `Bearer <token_sinh_vien>`

#### 6.5. Sinh viên tự cập nhật thông tin liên hệ
- **Method:** `PATCH`
- **URL:** `http://localhost:8080/api/sinh-vien/me/profile`
- **Headers:**
  - `Authorization`: `Bearer <token_sinh_vien>`
  - `Content-Type`: `application/json`
- **Body (raw JSON):**
  ```json
  {
    "email": "sv.nguyenvana@gmail.com",
    "soDienThoai": "0987654321",
    "diaChi": "Quận Cầu Giấy, Hà Nội"
  }
  ```
- **Kết quả trả về:** `200 OK`

#### 6.6. Thêm mới Sinh viên (Admin)
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/sinh-vien`
- **Headers:**
  - `Authorization`: `Bearer <token_admin>`
  - `Content-Type`: `application/json`
- **Body (raw JSON):**
  ```json
  {
    "mssv": "20240001",
    "hoTen": "Nguyễn Văn A",
    "ngaySinh": "2002-05-15",
    "gioiTinh": "Nam",
    "email": "nguyenvana@gmail.com",
    "soDienThoai": "0912345678",
    "diaChi": "Hà Nội",
    "ngayNhapHoc": "2024-09-05",
    "lopId": 1,
    "active": true
  }
  ```
  *(Hệ thống tự động sinh tài khoản login với username `20240001` và mật khẩu `15052002`)*
- **Kết quả trả về:** `201 Created`

#### 6.7. Cập nhật Sinh viên (Admin)
- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/sinh-vien/1`
- **Headers:**
  - `Authorization`: `Bearer <token_admin>`
  - `Content-Type`: `application/json`
- **Body (raw JSON):** Tương tự body thêm sinh viên.

#### 6.8. Reset mật khẩu Sinh viên về mặc định (Admin)
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/sinh-vien/1/reset-password`
- **Headers:** `Authorization`: `Bearer <token_admin>`
- **Kết quả trả về (200 OK):** Mật khẩu được trả về ngày sinh `ddMMyyyy`.

#### 6.9. Xóa Sinh viên (Admin)
- **Method:** `DELETE`
- **URL:** `http://localhost:8080/api/sinh-vien/1`
- **Headers:** `Authorization`: `Bearer <token_admin>`
- **Kết quả trả về:** `204 No Content`

---

### 7. NHÓM ĐỢT ĐÁNH GIÁ ĐRL (DOT-DANH-GIA)

#### 7.1. Lấy tất cả các đợt đánh giá
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/dot-danh-gia`
- **Headers:** `Authorization`: `Bearer <token_admin>`

#### 7.2. Lấy các đợt đang mở (Dành cho Sinh viên vào chấm điểm)
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/dot-danh-gia/open`
- **Headers:** `Authorization`: `Bearer <token>`

#### 7.3. Chi tiết đợt đánh giá theo ID
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/dot-danh-gia/1`
- **Headers:** `Authorization`: `Bearer <token>`

#### 7.4. Tạo đợt đánh giá mới (Admin)
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/dot-danh-gia`
- **Headers:**
  - `Authorization`: `Bearer <token_admin>`
  - `Content-Type`: `application/json`
- **Body (raw JSON):**
  ```json
  {
    "tenDot": "Đợt đánh giá ĐRL Học kỳ 1 Năm học 2023-2024",
    "hocKy": 1,
    "namHoc": "2023-2024",
    "ngayBatDau": "2024-01-01",
    "ngayKetThuc": "2024-02-28",
    "trangThai": "DANG_MO",
    "ghiChu": "Yêu cầu nộp kèm ảnh/link minh chứng"
  }
  ```
- **Kết quả trả về:** `201 Created`

#### 7.5. Cập nhật đợt đánh giá (Admin)
- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/dot-danh-gia/1`
- **Headers:**
  - `Authorization`: `Bearer <token_admin>`
  - `Content-Type`: `application/json`
- **Body (raw JSON):** Tương tự body tạo đợt đánh giá.

#### 7.6. Đóng / Mở nhanh đợt đánh giá (Toggle Status)
- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/dot-danh-gia/1/toggle`
- **Headers:** `Authorization`: `Bearer <token_admin>`
- **Kết quả trả về (200 OK):** Tự động đảo trạng thái giữa `DANG_MO` và `DONG`.

#### 7.7. Xóa đợt đánh giá
- **Method:** `DELETE`
- **URL:** `http://localhost:8080/api/dot-danh-gia/1`
- **Headers:** `Authorization`: `Bearer <token_admin>`
- **Kết quả trả về:** `204 No Content`

---

### 8. NHÓM ĐIỂM RÈN LUYỆN & BÁO CÁO PDF (DIEM-REN-LUYEN)

#### 8.1. Sinh viên nộp phiếu tự chấm điểm rèn luyện
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/diem-ren-luyen/submit`
- **Headers:**
  - `Authorization`: `Bearer <token_sinh_vien>`
  - `Content-Type`: `application/json`
- **Quy định thang điểm:**
  - `tieuChi1Sv`: [0 - 20] (Ý thức học tập)
  - `tieuChi2Sv`: [0 - 25] (Chấp hành nội quy)
  - `tieuChi3Sv`: [0 - 20] (Hoạt động phong trào, tình nguyện)
  - `tieuChi4Sv`: [0 - 25] (Phẩm chất công dân)
  - `tieuChi5Sv`: [0 - 10] (Ban cán sự, đoàn hội)
- **Body (raw JSON):**
  ```json
  {
    "dotDanhGiaId": 1,
    "hocKy": 1,
    "namHoc": "2023-2024",
    "tieuChi1Sv": 18,
    "tieuChi2Sv": 23,
    "tieuChi3Sv": 19,
    "tieuChi4Sv": 24,
    "tieuChi5Sv": 8,
    "ghiChuSinhVien": "Em tham gia phong trào đầy đủ",
    "minhChung": "https://drive.google.com/minh-chung-drl"
  }
  ```
- **Kết quả trả về:** `201 Created`

#### 8.2. Sinh viên xem danh sách phiếu của mình
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/diem-ren-luyen/my`
- **Headers:** `Authorization`: `Bearer <token_sinh_vien>`

#### 8.3. Admin tra cứu, lọc danh sách phiếu ĐRL
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/diem-ren-luyen/admin`
- **Các query param lọc (tùy chọn ghép vào URL):**
  - `dotDanhGiaId=1`
  - `trangThai=CHO_DUYET` *(hoặc `DA_DUYET`, `TU_CHOI`)*
  - `hocKy=1`
  - `namHoc=2023-2024`
  - `khoaId=1`
  - `lopId=1`
  - `keyword=Nguyen` *(tìm theo tên hoặc MSSV)*
- **Ví dụ URL đầy đủ:**  
  `http://localhost:8080/api/diem-ren-luyen/admin?dotDanhGiaId=1&trangThai=CHO_DUYET`
- **Headers:** `Authorization`: `Bearer <token_admin>`

#### 8.4. Xem chi tiết 1 phiếu ĐRL
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/diem-ren-luyen/1`
- **Headers:** `Authorization`: `Bearer <token>`

#### 8.5. Admin Phê duyệt phiếu ĐRL (Duyệt)
- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/diem-ren-luyen/1/duyet`
- **Headers:**
  - `Authorization`: `Bearer <token_admin>`
  - `Content-Type`: `application/json`
- **Body (raw JSON):**
  ```json
  {
    "chapNhan": true,
    "tieuChi1Admin": 18,
    "tieuChi2Admin": 22,
    "tieuChi3Admin": 18,
    "tieuChi4Admin": 23,
    "tieuChi5Admin": 8,
    "nhanXetAdmin": "Minh chứng hợp lệ, duyệt điểm xếp loại Tốt"
  }
  ```
- **Kết quả trả về:** `200 OK` *(Tự tính tổng điểm và xếp loại: Xuất sắc / Tốt / Khá / Trung bình)*

#### 8.6. Admin Từ chối phiếu ĐRL (Từ chối)
- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/diem-ren-luyen/1/duyet`
- **Headers:**
  - `Authorization`: `Bearer <token_admin>`
  - `Content-Type`: `application/json`
- **Body (raw JSON):**
  ```json
  {
    "chapNhan": false,
    "nhanXetAdmin": "Minh chứng không khớp với tiêu chí 3"
  }
  ```
- **Kết quả trả về:** `200 OK`

#### 8.7. Xuất file PDF Phiếu ĐRL cá nhân (JasperReports)
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/diem-ren-luyen/1/export-pdf`
- **Headers:** `Authorization`: `Bearer <token>`
- **Cách tải trên Postman:** Bấm vào mũi tên nhỏ cạnh nút **Send** -> chọn **"Send and Download"** để tải file `.pdf` về máy.

#### 8.8. Xuất file PDF Bảng tổng hợp ĐRL
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/diem-ren-luyen/export-summary-pdf?namHoc=2023-2024&hocKy=1`
- **Headers:** `Authorization`: `Bearer <token>`
- **Cách tải trên Postman:** Bấm vào mũi tên nhỏ cạnh nút **Send** -> chọn **"Send and Download"**.

#### 8.9. Thống kê tổng số phiếu ĐRL theo trạng thái
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/diem-ren-luyen/thong-ke`
- **Headers:** `Authorization`: `Bearer <token_admin>`
- **Kết quả trả về (200 OK):** Trả về `tongSoPhieu`, `soChoDuyet`, `soDaDuyet`, `soTuChoi` và `phanBoXepLoai`.

#### 8.10. Xóa phiếu ĐRL
- **Method:** `DELETE`
- **URL:** `http://localhost:8080/api/diem-ren-luyen/1`
- **Headers:** `Authorization`: `Bearer <token_admin>`
- **Kết quả trả về:** `204 No Content`
