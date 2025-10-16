package com.investment.metal.infrastructure.persistence.adapter;

import com.investment.metal.domain.model.Notification;
import com.investment.metal.domain.repository.NotificationRepository;
import com.investment.metal.infrastructure.mapper.NotificationMapper;
import com.investment.metal.infrastructure.persistence.repository.NotificationJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Adapter that exposes the domain-facing {@link NotificationRepository}
 * backed by the Spring Data JPA repository.
 */
@Component
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationMapper notificationMapper;

    public NotificationRepositoryAdapter(
            NotificationJpaRepository notificationJpaRepository,
            NotificationMapper notificationMapper) {
        this.notificationJpaRepository = notificationJpaRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    public Optional<Notification> findByUserId(Integer userId) {
        return notificationJpaRepository.findByUserId(userId)
                .map(notificationMapper::toDomain);
    }

    @Override
    public List<Notification> findAll() {
        return notificationJpaRepository.findAll()
                .stream()
                .map(notificationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Notification save(Notification notification) {
        var entity = notificationMapper.toEntity(notification);
        var saved = notificationJpaRepository.save(entity);
        return notificationMapper.toDomain(saved);
    }

    @Override
    public void deleteByUserId(Integer userId) {
        notificationJpaRepository.deleteByUserId(userId);
    }
}
