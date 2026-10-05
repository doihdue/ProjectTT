package com.demo.be.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventMessage implements Serializable {

    private String eventType; // DRL_SUBMITTED, DRL_APPROVED, DRL_REJECTED
    private String recipient;
    private String mssv;
    private String hoTen;
    private Integer hocKy;
    private String namHoc;
    private Integer tongDiem;
    private String xepLoai;
    private String reason;
    private String nguoiDuyet;
    private String title;
    private String content;
    private String link;

    @Builder.Default
    private String timestamp = LocalDateTime.now().toString();
}
