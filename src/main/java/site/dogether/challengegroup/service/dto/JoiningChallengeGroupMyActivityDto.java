package site.dogether.challengegroup.service.dto;

public record JoiningChallengeGroupMyActivityDto(
        int totalTodoCount,
        int totalCertificatedCount,
        int totalApprovedCount,
        int totalRejectedCount
) {
}
