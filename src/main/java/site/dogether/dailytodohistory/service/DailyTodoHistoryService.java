package site.dogether.dailytodohistory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.challengegroup.repository.ChallengeGroupRepository;
import site.dogether.dailytodo.entity.DailyTodo;
import site.dogether.dailytodo.repository.DailyTodoRepository;
import site.dogether.dailytodocertification.entity.DailyTodoCertification;
import site.dogether.dailytodohistory.entity.DailyTodoHistory;
import site.dogether.dailytodohistory.repository.DailyTodoHistoryReadRepository;
import site.dogether.dailytodohistory.repository.DailyTodoHistoryRepository;
import site.dogether.member.repository.MemberRepository;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DailyTodoHistoryService {

    private final ChallengeGroupRepository challengeGroupRepository;
    private final MemberRepository memberRepository;
    private final DailyTodoRepository dailyTodoRepository;
    private final DailyTodoHistoryRepository dailyTodoHistoryRepository;
    private final DailyTodoHistoryReadRepository dailyTodoHistoryReadRepository;

    @Transactional
    public void saveDailyTodoWriteHistory(final List<DailyTodo> dailyTodos) {
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
    public void saveDailyTodoHistoryWithCertification(final DailyTodo dailyTodo, final DailyTodoCertification dailyTodoCertification) {
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

//    public List<FindTargetMemberTodayTodoHistoriesDto> findTargetMemberTodayTodoHistories(
//        final Long viewerId,
//        final Long challengeGroupId,
//        final Long targetMemberId
//    ) {
//        final ChallengeGroup challengeGroup = getChallengeGroup(challengeGroupId);
//        final Member viewer = getMember(viewerId);
//        final Member targetMember = getMember(targetMemberId);
//        final LocalDate todayDate = LocalDate.now();
//        final List<DailyTodoHistory> dailyTodoHistories = dailyTodoHistoryRepository.findAllByChallengeGroupAndMemberAndEventAtBetween(
//            challengeGroup,
//            targetMember,
//            todayDate.atStartOfDay(),
//            todayDate.atTime(LocalTime.MAX)
//        );
//
//        return convertDtoFromDailyTodoHistories(dailyTodoHistories, viewer);
//    }
//
//    private ChallengeGroup getChallengeGroup(final Long challengeGroupId) {
//        return challengeGroupRepository.findById(challengeGroupId)
//            .orElseThrow(() -> new ChallengeGroupNotFoundException(String.format("존재하지 않는 챌린지 그룹 id입니다. (%d)", challengeGroupId)));
//    }
//
//    private Member getMember(final Long memberId) {
//        return memberRepository.findById(memberId)
//            .orElseThrow(() -> new MemberNotFoundException(String.format("존재하지 않는 회원 id입니다. (%d)", memberId)));
//    }
//
//    private List<TodoDto> convertDtoFromDailyTodoHistories(final List<DailyTodoHistory> histories) {
//        histories.stream()
//            .map(history -> )
//    }
//
//    private TodoDto convertDtoFromHistory(final DailyTodoHistory history, final Member viewer) {
//        final DailyTodoHistoryType historyType = history.getHistoryType();
//        final DailyTodo dailyTodo = history.getDailyTodo();
//
//        if (historyType == WRITE) {
//            return new TodoDto(dailyTodo)
//        }
//    }
//
//    private boolean checkDailyTodoHistoryRead(final DailyTodoHistory dailyTodoHistory, final Member viewer) {
//        return dailyTodoHistoryReadRepository.existsByMemberAndDailyTodoHistory(viewer, dailyTodoHistory);
//    }
}
