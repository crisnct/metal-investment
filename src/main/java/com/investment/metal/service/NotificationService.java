package com.investment.metal.service;

import com.investment.metal.MessageKey;
import com.investment.metal.database.Customer;
import com.investment.metal.database.Notification;
import com.investment.metal.database.NotificationRepository;
import com.investment.metal.database.Purchase;
import com.investment.metal.dto.UserMetalInfoDto;
import com.investment.metal.exceptions.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationService extends AbstractService {

    public static final long MIN_NOTIFICATION_PERIOD = TimeUnit.DAYS.toMillis(1);

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private MetalPriceService metalPricesService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationRepository notificationRepository;

    public void save(Integer userId, int period) {
        Notification entity = this.notificationRepository
                .findByUserId(userId)
                .orElse(new Notification());
        entity.setUserId(userId);
        entity.setFrequency(period);
        entity.setLastTimeNotified(new Timestamp(System.currentTimeMillis()));
        this.notificationRepository.save(entity);
    }

    public void notifyUser(Integer userId) {
        Customer user = this.accountService.findById(userId);
        this.notifyUser(user);
    }

    public void notifyUser(Customer user) {
        List<Purchase> purchases = this.purchaseService.getAllPurchase(user.getId());
        if (purchases.isEmpty()) {
            throw this.exceptionService
                    .createBuilder(MessageKey.INVALID_REQUEST)
                    .setArguments("The user didn't have any purchase")
                    .build();
        } else {
            Map<String, UserMetalInfoDto> userProfit = new HashMap<>();
            for (Purchase purchase : purchases) {
                final UserMetalInfoDto info = this.metalPricesService.calculatesUserProfit(purchase);
                userProfit.put(info.getMetalSymbol(), info);
            }
            this.emailService.sendStatusNotification(user, userProfit);
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
        final Instant currentTime = (new Timestamp(System.currentTimeMillis())).toInstant();
        Instant notificationLastTime = notification.getLastTimeNotified().toInstant();
        Instant nextTimeNotify = notificationLastTime.plusMillis(notification.getFrequency());
        if (nextTimeNotify.isBefore(currentTime)) {
            try {
                this.notifyUser(notification.getUserId());
                this.save(notification.getUserId(), notification.getFrequency());
            } catch (BusinessException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public int getNotificationFrequency(Integer userId) {
        final Notification notification = this.notificationRepository
                .findByUserId(userId)
                .orElseGet(() -> {
                    Notification empty = new Notification();
                    empty.setFrequency(0);
                    return empty;
                });
        return notification.getFrequency();
    }
}
