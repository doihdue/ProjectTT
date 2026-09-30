package com.demo.be.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "thong_bao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThongBao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String recipientUsername;

    @Column(nullable = false, length = 255, columnDefinition = "NVARCHAR(255)")
    private String tieuDe;

    @Column(nullable = false, length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String noiDung;

    @Column(length = 50)
    private String loaiThongBao;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime thoiGianTao = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private boolean daDoc = false;

    @Column(length = 255)
    private String lienKet;
}
