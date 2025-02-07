package site.dogether.notification.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;
import site.dogether.notification.infrastructure.entity.NotificationTokenJpaEntity;

import java.util.List;
import java.util.Optional;

public interface NotificationTokenJpaRepository extends JpaRepository<NotificationTokenJpaEntity, Long> {

    List<NotificationTokenJpaEntity> findAllByMember_Id(Long memberId);

    void deleteAllByValue(String value);

    boolean existsByMemberAndValue(MemberJpaEntity member, String value);

    Optional<NotificationTokenJpaEntity> findByMemberAndValue(MemberJpaEntity member, String value);
}
