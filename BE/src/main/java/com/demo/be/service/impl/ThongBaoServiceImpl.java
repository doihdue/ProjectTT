package com.demo.be.service.impl;

import com.demo.be.dto.message.NotificationEventMessage;
import com.demo.be.model.ThongBao;
import com.demo.be.repository.ThongBaoRepository;
import com.demo.be.service.ThongBaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThongBaoServiceImpl implements ThongBaoService {

    private final ThongBaoRepository thongBaoRepository;

    @Override
    public List<ThongBao> getMyNotifications(String username) {
        return thongBaoRepository.findByRecipientUsernameOrderByThoiGianTaoDesc(username);
    }

    @Override
    public long getUnreadCount(String username) {
        return thongBaoRepository.countByRecipientUsernameAndDaDocFalse(username);
    }

    @Override
    @Transactional
    public void markAsRead(Long id, String username) {
        thongBaoRepository.findById(id).ifPresent(tb -> {
            if (tb.getRecipientUsername().equalsIgnoreCase(username)) {
                tb.setDaDoc(true);
                thongBaoRepository.save(tb);
            }
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(String username) {
        thongBaoRepository.markAllAsReadByUsername(username);
    }

    @Override
    @Transactional
    public void createNotification(String recipient, String title, String content, String type, String link) {
        ThongBao tb = ThongBao.builder()
                .recipientUsername(recipient)
                .tieuDe(title)
                .noiDung(content)
                .loaiThongBao(type)
                .thoiGianTao(LocalDateTime.now())
                .daDoc(false)
                .lienKet(link)
                .build();
        thongBaoRepository.save(tb);
    }

    @Override
    @Transactional
    public void processNotificationEvent(NotificationEventMessage event) {
        if (event == null || event.getEventType() == null) return;

        switch (event.getEventType()) {
            case "DRL_SUBMITTED":
                createNotification(
                        "admin",
                        "📋 Phiếu đánh giá ĐRL mới chờ duyệt",
                        "Sinh viên " + event.getHoTen() + " (" + event.getMssv() + ") vừa nộp phiếu tự đánh giá ĐRL Học kỳ "
                                + event.getHocKy() + " Năm học " + event.getNamHoc() + " (Tự chấm: " + event.getTongDiem() + " điểm).",
                        "DRL_CHO_DUYET",
                        "/quan-ly/diem-ren-luyen"
                );
                log.info("Đã tạo thông báo phiếu ĐRL chờ duyệt gửi Admin.");
                break;

            case "DRL_APPROVED":
                String studentUser = event.getMssv() != null ? event.getMssv() : event.getRecipient();
                createNotification(
                        studentUser,
                        "✅ Điểm rèn luyện đã được phê duyệt",
                        "Phiếu đánh giá ĐRL Học kỳ " + event.getHocKy() + " Năm học " + event.getNamHoc()
                                + " của bạn đã được Admin phê duyệt: " + event.getTongDiem() + " điểm (Xếp loại: " + event.getXepLoai() + ")."
                                + (event.getReason() != null && !event.getReason().isBlank() ? " Nhận xét: " + event.getReason() : ""),
                        "DRL_DUYET",
                        "/sinh-vien/ket-qua-drl"
                );
                log.info("Đã tạo thông báo ĐRL đã duyệt cho sinh viên: {}", studentUser);
                break;

            case "DRL_REJECTED":
                String rejectUser = event.getMssv() != null ? event.getMssv() : event.getRecipient();
                createNotification(
                        rejectUser,
                        "⚠️ Phiếu điểm rèn luyện cần chỉnh sửa",
                        "Phiếu đánh giá ĐRL Học kỳ " + event.getHocKy() + " Năm học " + event.getNamHoc()
                                + " của bạn đã bị từ chối với lý do: \"" + (event.getReason() != null ? event.getReason() : "Vui lòng bổ sung minh chứng")
                                + "\". Bạn vui lòng kiểm tra và gửi đánh giá lại.",
                        "DRL_TU_CHOI",
                        "/sinh-vien/danh-gia-drl"
                );
                log.info("Đã tạo thông báo từ chối ĐRL cho sinh viên: {}", rejectUser);
                break;

            default:
                if (event.getRecipient() != null && event.getTitle() != null) {
                    createNotification(
                            event.getRecipient(),
                            event.getTitle(),
                            event.getContent() != null ? event.getContent() : "",
                            event.getEventType(),
                            event.getLink()
                    );
                }
                break;
        }
    }
}
