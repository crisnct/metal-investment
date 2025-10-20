package com.investment.metal.application.service;

import com.investment.metal.MessageKey;
import com.investment.metal.application.dto.UserMetalInfoDto;
import com.investment.metal.domain.exception.BusinessException;
import com.investment.metal.domain.model.MetalPurchase;
import com.investment.metal.domain.model.Notification;
import com.investment.metal.domain.model.User;
import com.investment.metal.domain.repository.NotificationRepository;
import com.investment.metal.infrastructure.exception.ExceptionService;
import com.investment.metal.infrastructure.mapper.UserMapper;
import com.investment.metal.infrastructure.persistence.entity.Customer;
import com.investment.metal.infrastructure.service.AccountService;
import com.investment.metal.infrastructure.service.EmailService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class NotificationService {

    /**
     * Exception service for handling business exceptions
     */
    @Autowired
    private ExceptionService exceptionService;

    /**
     * Mapper for converting between domain models and infrastructure entities
     */
    @Autowired
    private UserMapper userMapper;

    public static final long MIN_NOTIFICATION_PERIOD = TimeUnit.DAYS.toMillis(1);

    @Autowired
    private com.investment.metal.application.service.MetalPriceService metalPricesService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationRepository notificationRepository;

    public void save(Integer userId, int period) {
        Notification notification = this.notificationRepository
                .findByUserId(userId)
                .orElseGet(() -> Notification.builder().userId(userId).build());
        notification.setFrequency(period);
        notification.setLastTimeNotified(LocalDateTime.now());
        this.notificationRepository.save(notification);
    }

    public void notifyUser(Integer userId) {
        Customer customerEntity = this.accountService.findById(userId);
        User user = userMapper.toDomainModel(customerEntity);
        this.notifyUser(user);
    }

    public void notifyUser(User user) {
        List<MetalPurchase> purchases = this.purchaseService.getAllPurchase(user.getId());
        if (purchases.isEmpty()) {
            throw this.exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("The user didn't have any purchase")
                    .build();
        } else {
            Map<String, UserMetalInfoDto> userProfit = new HashMap<>();
            for (MetalPurchase purchase : purchases) {
                final UserMetalInfoDto info = this.metalPricesService.calculatesUserProfit(purchase);
                userProfit.put(info.getMetalSymbol(), info);
            }
            // Convert User domain model to Customer entity for email service
            Customer customerEntity = userMapper.toEntity(user);
            this.emailService.sendStatusNotification(customerEntity, userProfit);
        }
    }

    @Transactional
    public void checkNotifications() {
        this.notificationRepository.findAll()
                .stream()
                .filter(p -> p.getFrequency() > 0)
                .forEach(this::checkNotification);
    }

    private void checkNotification(Notification notification) {
        if (notification.getLastTimeNotified() == null ||
                notification.getLastTimeNotified().plusDays(notification.getFrequency()).isBefore(LocalDateTime.now())) {
            try {
                this.notifyUser(notification.getUserId());
                this.save(notification.getUserId(), notification.getFrequency());
            } catch (BusinessException e) {
                log.error("Failed to notify user {} during scheduled notification check", notification.getUserId(), e);
            }
        }
    }

    public int getNotificationFrequency(Integer userId) {
        final Notification notification = this.notificationRepository
                .findByUserId(userId)
                .orElseGet(() -> Notification.builder().frequency(0).build());
        return notification.getFrequency();
    }
}
