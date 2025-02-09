package site.dogether.challengegroup.service.exception;

public class MemberNotInChallengeGroupException extends RuntimeException {

    public MemberNotInChallengeGroupException(final String message) {
        super(message);
    }
}
