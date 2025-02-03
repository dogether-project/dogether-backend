package site.dogether.notification.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.notification.infrastructure.entity.NotificationTokenJpaEntity;

public interface NotificationTokenJpaRepository extends JpaRepository<NotificationTokenJpaEntity, Long> {
}
