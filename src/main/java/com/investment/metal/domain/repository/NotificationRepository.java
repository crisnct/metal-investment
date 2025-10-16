package com.investment.metal.domain.repository;

import com.investment.metal.domain.model.Notification;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository abstraction for {@link Notification} aggregate roots.
 */
public interface NotificationRepository {

    Optional<Notification> findByUserId(Integer userId);

    List<Notification> findAll();

    Notification save(Notification notification);

    void deleteByUserId(Integer userId);
}
