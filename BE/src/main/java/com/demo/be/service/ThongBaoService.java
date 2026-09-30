package com.demo.be.service;

import com.demo.be.dto.message.NotificationEventMessage;
import com.demo.be.model.ThongBao;
import java.util.List;

public interface ThongBaoService {

    List<ThongBao> getMyNotifications(String username);

    long getUnreadCount(String username);

    void markAsRead(Long id, String username);

    void markAllAsRead(String username);

    void createNotification(String recipient, String title, String content, String type, String link);

    void processNotificationEvent(NotificationEventMessage event);
}
