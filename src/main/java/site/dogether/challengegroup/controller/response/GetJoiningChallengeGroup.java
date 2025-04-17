package site.dogether.challengegroup.controller.response;

public record GetJoiningChallengeGroup (
    String groupName,
    String joinCode,
    String endAt,
    int currentDay
){
}
