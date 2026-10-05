package com.demo.be.producer;

import com.demo.be.config.RabbitMQConfig;
import com.demo.be.dto.message.NotificationEventMessage;
import com.demo.be.service.ThongBaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ThongBaoService thongBaoService;

    @Value("${app.rabbitmq.enabled:false}")
    private boolean rabbitMqEnabled;

    public void sendNotification(NotificationEventMessage event) {
        if (!rabbitMqEnabled) {
            log.info("[NOTIFICATION] RabbitMQ chưa được bật (Docker chưa chạy). Lưu thông báo trực tiếp vào CSDL cho [{}]", event.getRecipient());
            thongBaoService.processNotificationEvent(event);
            return;
        }

        log.info("[RABBITMQ PRODUCER] ===> Đang đẩy message sự kiện [{}] cho recipient [{}] vào Exchange: {}",
                event.getEventType(), event.getRecipient(), RabbitMQConfig.EXCHANGE_NAME);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_GRADE,
                    event
            );
            log.info("[RABBITMQ PRODUCER] ===> Gửi message thành công tới RabbitMQ Queue: {}", RabbitMQConfig.QUEUE_NAME);
        } catch (AmqpException e) {
            log.warn("[RABBITMQ PRODUCER] [FALLBACK] Lỗi khi gửi tới RabbitMQ ({}). Tự động chuyển sang xử lý trực tiếp nội bộ...", e.getMessage());
            // Fallback an toàn: vẫn lưu thông báo cho sinh viên & giảng viên mà không gây lỗi request
            thongBaoService.processNotificationEvent(event);
        }
    }
}
