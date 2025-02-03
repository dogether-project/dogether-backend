package site.dogether.member.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {
}
