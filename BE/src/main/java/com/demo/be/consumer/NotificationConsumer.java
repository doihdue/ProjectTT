package com.demo.be.consumer;

import com.demo.be.config.RabbitMQConfig;
import com.demo.be.dto.message.NotificationEventMessage;
import com.demo.be.service.ThongBaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true")
@RequiredArgsConstructor
public class NotificationConsumer {

    private final ThongBaoService thongBaoService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveNotificationMessage(NotificationEventMessage event) {
        log.info("[RABBITMQ CONSUMER] <=== Đã nhận được message từ Queue [{}]: Sự kiện = {}, Sinh viên = {}",
                RabbitMQConfig.QUEUE_NAME, event.getEventType(), event.getMssv());

        thongBaoService.processNotificationEvent(event);

        log.info("[RABBITMQ CONSUMER] <=== Đã xử lý xong thông báo ĐRL thành công!");
    }
}
