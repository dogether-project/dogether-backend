package site.dogether.challengegroup.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.entity.ChallengeGroupStatus;
import site.dogether.challengegroup.exception.ChallengeGroupNotFoundException;
import site.dogether.challengegroup.exception.JoiningChallengeGroupNotFoundException;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Component
public class ChallengeGroupReader {

    private final ChallengeGroupRepository challengeGroupRepository;
    
    public ChallengeGroup getById(final Long challengeGroupId) {
        return challengeGroupRepository.findById(challengeGroupId)
            .orElseThrow(() -> new ChallengeGroupNotFoundException(
                String.format("존재하지 않는 챌린지 그룹 id입니다. (%d)", challengeGroupId)));
    }
    
    public ChallengeGroup getByJoinCode(final String joinCode) {
        return challengeGroupRepository.findByJoinCode_Value(joinCode)
            .orElseThrow(() -> new JoiningChallengeGroupNotFoundException(
                String.format("참여하려는 챌린지 그룹이 존재하지 않습니다. (%s)", joinCode)));
    }
    
    public List<ChallengeGroup> findByStatusNot(final ChallengeGroupStatus status) {
        return challengeGroupRepository.findByStatusNot(status);
    }
}
