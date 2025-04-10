package site.dogether.member.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderId(String providerId);
}
