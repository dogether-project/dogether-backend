package site.dogether.notification.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.member.entity.Member;
import site.dogether.notification.entity.NotificationToken;

public interface NotificationTokenRepository extends JpaRepository<NotificationToken, Long> {

    List<NotificationToken> findAllByMember_Id(Long memberId);

    void deleteAllByValue(String value);

    boolean existsByMemberAndValue(Member member, String value);

    Optional<NotificationToken> findByMemberAndValue(Member member, String value);

    List<NotificationToken> findAllByMember(Member member);
}
