package site.dogether.challengegroup.service.exception;

public class NotEnoughChallengeGroupMembersException extends RuntimeException {

    public NotEnoughChallengeGroupMembersException(final String message) {
        super(message);
    }
}
