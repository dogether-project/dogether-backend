package site.dogether.dailytodohistory.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.exception.ChallengeGroupNotFoundException;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodocertification.repository.DailyTodoCertificationRepository;
import site.dogether.dailytodohistory.entity.DailyTodoHistory;
import site.dogether.dailytodohistory.entity.DailyTodoHistoryRead;
import site.dogether.dailytodohistory.entity.DailyTodoHistoryReadStatus;
import site.dogether.dailytodohistory.exception.DailyTodoHistoryAlreadyReadException;
import site.dogether.dailytodohistory.exception.DailyTodoHistoryNotFoundException;
import site.dogether.dailytodohistory.repository.DailyTodoHistoryReadRepository;
import site.dogether.dailytodohistory.repository.DailyTodoHistoryRepository;
import site.dogether.dailytodohistory.service.dto.FindTargetMemberTodayTodoHistoriesDto;
import site.dogether.dailytodohistory.service.dto.TodoHistoryDto;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DailyTodoHistoryService {

    private final ChallengeGroupRepository challengeGroupRepository;
    private final MemberRepository memberRepository;
    private final DailyTodoHistoryRepository dailyTodoHistoryRepository;
    private final DailyTodoHistoryReadRepository dailyTodoHistoryReadRepository;
    private final DailyTodoCertificationRepository dailyTodoCertificationRepository;

    @Transactional
    public void initDailyTodoHistories(final List<DailyTodo> dailyTodos) {
        final List<DailyTodoHistory> dailyTodoHistories = dailyTodos.stream()
            .map(DailyTodoHistory::new)
            .toList();
        dailyTodoHistoryRepository.saveAll(dailyTodoHistories);
    }

    @Transactional
    public void updateDailyTodoHistory(final DailyTodo dailyTodo) {
        final DailyTodoHistory dailyTodoHistory = dailyTodoHistoryRepository.findByDailyTodo(dailyTodo)
            .orElseThrow(() -> new DailyTodoHistoryNotFoundException(String.format("데일리 투두의 히스토리가 존재하지 않습니다. (%s)", dailyTodo)));
        dailyTodoHistory.updateEventTime();
        dailyTodoHistoryReadRepository.deleteAllByDailyTodoHistory(dailyTodoHistory);
    }

    public FindTargetMemberTodayTodoHistoriesDto findAllTodayTodoHistories(
        final Long viewerId,
        final Long challengeGroupId,
        final Long targetMemberId
    ) {
        final ChallengeGroup challengeGroup = getChallengeGroup(challengeGroupId);
        final Member viewer = getMember(viewerId);
        final Member targetMember = getMember(targetMemberId);
        final List<DailyTodoHistory> dailyTodoHistories = findAllHistoryOfWrittenTodayTodoByChallengeGroupAndTargetMember(challengeGroup, targetMember);
        final List<TodoHistoryDto> todoHistoryDtos = dailyTodoHistories.stream()
            .map(history -> convertDtoFromHistory(history, viewer))
            .toList();
        int currentTodoHistoryToReadIndex = calculateCurrentTodoHistoryToReadIndex(todoHistoryDtos);

        return new FindTargetMemberTodayTodoHistoriesDto(currentTodoHistoryToReadIndex, todoHistoryDtos);
    }

    private ChallengeGroup getChallengeGroup(final Long challengeGroupId) {
        return challengeGroupRepository.findById(challengeGroupId)
            .orElseThrow(() -> new ChallengeGroupNotFoundException(String.format("존재하지 않는 챌린지 그룹 id입니다. (%d)", challengeGroupId)));
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원 id입니다. (%d)", memberId)));
    }

    private List<DailyTodoHistory> findAllHistoryOfWrittenTodayTodoByChallengeGroupAndTargetMember(final ChallengeGroup challengeGroup, final Member targetMember) {
        final LocalDate todayDate = LocalDate.now();
        return dailyTodoHistoryRepository.findAllByDailyTodo_ChallengeGroupAndDailyTodo_MemberAndDailyTodo_WrittenAtBetweenOrderByEventTimeAsc(
            challengeGroup,
            targetMember,
            todayDate.atStartOfDay(),
            todayDate.atTime(LocalTime.MAX)
        );
    }

    private TodoHistoryDto convertDtoFromHistory(final DailyTodoHistory history, final Member viewer) {
        final DailyTodo dailyTodo = history.getDailyTodo();
        final boolean isHistoryRead = checkMemberReadDailyTodoHistory(viewer, history);
        return dailyTodoCertificationRepository.findByDailyTodo(dailyTodo)
            .map(dailyTodoCertification -> new TodoHistoryDto(
                history.getId(),
                dailyTodo.getContent(),
                dailyTodoCertification.getReviewStatus().name(),
                dailyTodoCertification.getContent(),
                dailyTodoCertification.getMediaUrl(),
                isHistoryRead,
                dailyTodoCertification.findReviewFeedback().orElse(null)))
            .orElse(new TodoHistoryDto(
                history.getId(),
                dailyTodo.getContent(),
                dailyTodo.getStatus().name(),
                null,
                null,
                isHistoryRead,
                    null));
    }

    private boolean checkMemberReadDailyTodoHistory(final Member member, final DailyTodoHistory dailyTodoHistory) {
        return dailyTodoHistoryReadRepository.existsByMemberAndDailyTodoHistory(member, dailyTodoHistory);
    }

    private int calculateCurrentTodoHistoryToReadIndex(final List<TodoHistoryDto> todoHistoryDtos) {
        for (int i = 0; i < todoHistoryDtos.size(); i++) {
            if (!todoHistoryDtos.get(i).isRead()) {
                return i;
            }
        }

        return todoHistoryDtos.size() - 1;
    }

    @Transactional
    public void saveDailyTodoHistoryRead(final Long memberId, final Long dailyTodoHistoryId) {
        final Member member = getMember(memberId);
        final DailyTodoHistory dailyTodoHistory = getDailyTodoHistory(dailyTodoHistoryId);

        validateDailyTodoHistoryNotAlreadyRead(member, dailyTodoHistory);
        final DailyTodoHistoryRead dailyTodoHistoryRead = new DailyTodoHistoryRead(member, dailyTodoHistory);
        dailyTodoHistoryReadRepository.save(dailyTodoHistoryRead);
    }

    private DailyTodoHistory getDailyTodoHistory(final Long dailyTodoHistoryId) {
        return dailyTodoHistoryRepository.findById(dailyTodoHistoryId)
            .orElseThrow(() -> new DailyTodoHistoryNotFoundException(String.format("존재하지 않는 데일리 투두 히스토리 id입니다. (%d)", dailyTodoHistoryId)));
    }

    private void validateDailyTodoHistoryNotAlreadyRead(final Member member, final DailyTodoHistory dailyTodoHistory) {
        final boolean isHistoryRead = dailyTodoHistoryReadRepository.existsByMemberAndDailyTodoHistory(member, dailyTodoHistory);
        if (isHistoryRead) {
            throw new DailyTodoHistoryAlreadyReadException(String.format("이미 읽음 처리한 투두 히스토리 입니다. (%s) (%s)", member, dailyTodoHistory));
        }
    }

    public DailyTodoHistoryReadStatus getTodayDailyTodoHistoryReadStatus(
        final Member viewer,
        final ChallengeGroup challengeGroup,
        final Member targetMember
    ) {
        final List<DailyTodoHistory> dailyTodoHistories = findAllHistoryOfWrittenTodayTodoByChallengeGroupAndTargetMember(challengeGroup, targetMember);

        if (dailyTodoHistories.isEmpty()) {
            return DailyTodoHistoryReadStatus.NULL;
        }

        for (DailyTodoHistory dailyTodoHistory : dailyTodoHistories) {
            if (!checkMemberReadDailyTodoHistory(viewer, dailyTodoHistory)) {
                return DailyTodoHistoryReadStatus.READ_YET;
            }
        }

        return DailyTodoHistoryReadStatus.READ_ALL;
    }
}
