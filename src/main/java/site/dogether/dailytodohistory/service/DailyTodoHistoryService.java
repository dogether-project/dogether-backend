package site.dogether.dailytodohistory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.entity.ChallengeGroup;
import site.dogether.challengegroup.exception.ChallengeGroupNotFoundException;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodohistory.entity.DailyTodoHistory;
import site.dogether.dailytodohistory.repository.DailyTodoHistoryReadRepository;
import site.dogether.dailytodohistory.repository.DailyTodoHistoryRepository;
import site.dogether.dailytodohistory.service.dto.FindTargetMemberTodayTodoHistoriesDto;
import site.dogether.dailytodohistory.service.dto.TodoHistoryDto;
import site.dogether.member.entity.Member;
import site.dogether.member.exception.MemberNotFoundException;
import site.dogether.member.repository.MemberRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DailyTodoHistoryService {

    private final ChallengeGroupRepository challengeGroupRepository;
    private final MemberRepository memberRepository;
    private final DailyTodoHistoryRepository dailyTodoHistoryRepository;
    private final DailyTodoHistoryReadRepository dailyTodoHistoryReadRepository;

    @Transactional
    public void saveDailyTodoHistories(final List<DailyTodo> dailyTodos) {
        final List<DailyTodoHistory> dailyTodoHistories = dailyTodos.stream()
            .map(dailyTodo -> new DailyTodoHistory(
                dailyTodo.getChallengeGroup(),
                dailyTodo.getMember(),
                dailyTodo.getContent(),
                dailyTodo.getStatus(),
                null,
                null
            ))
            .toList();
        dailyTodoHistoryRepository.saveAll(dailyTodoHistories);
    }

    @Transactional
    public void saveDailyTodoHistory(final DailyTodo dailyTodo, final DailyTodoCertification dailyTodoCertification) {
        final DailyTodoHistory dailyTodoHistory = new DailyTodoHistory(
            dailyTodo.getChallengeGroup(),
            dailyTodo.getMember(),
            dailyTodo.getContent(),
            dailyTodo.getStatus(),
            dailyTodoCertification.getContent(),
            dailyTodoCertification.getMediaUrl()
        );
        dailyTodoHistoryRepository.save(dailyTodoHistory);
    }

    public FindTargetMemberTodayTodoHistoriesDto findTargetMemberTodayTodoHistories(
        final Long viewerId,
        final Long challengeGroupId,
        final Long targetMemberId
    ) {
        final ChallengeGroup challengeGroup = getChallengeGroup(challengeGroupId);
        final Member viewer = getMember(viewerId);
        final Member targetMember = getMember(targetMemberId);
        final LocalDate todayDate = LocalDate.now();
        final List<DailyTodoHistory> dailyTodoHistories = dailyTodoHistoryRepository.findAllByChallengeGroupAndMemberAndEventAtBetween(
            challengeGroup,
            targetMember,
            todayDate.atStartOfDay(),
            todayDate.atTime(LocalTime.MAX)
        );

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

    private TodoHistoryDto convertDtoFromHistory(final DailyTodoHistory history, final Member viewer) {
        final boolean isHistoryRead = dailyTodoHistoryReadRepository.existsByMemberAndDailyTodoHistory(viewer, history);
        return TodoHistoryDto.fromTodoHistory(history, isHistoryRead);
    }

    private int calculateCurrentTodoHistoryToReadIndex(final List<TodoHistoryDto> todoHistoryDtos) {
        for (int i = 0; i < todoHistoryDtos.size(); i++) {
            if (!todoHistoryDtos.get(i).isRead()) {
                return i;
            }
        }

        return todoHistoryDtos.size() - 1;
    }
}
