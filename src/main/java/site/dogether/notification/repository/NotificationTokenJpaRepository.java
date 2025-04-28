package site.dogether.notification.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.member.entity.Member;
import site.dogether.notification.entity.NotificationTokenJpaEntity;

public interface NotificationTokenJpaRepository extends JpaRepository<NotificationTokenJpaEntity, Long> {

    List<NotificationTokenJpaEntity> findAllByMember_Id(Long memberId);

    void deleteAllByValue(String value);

    boolean existsByMemberAndValue(Member member, String value);

    Optional<NotificationTokenJpaEntity> findByMemberAndValue(Member member, String value);

    List<NotificationTokenJpaEntity> findAllByMember(Member member);
}
