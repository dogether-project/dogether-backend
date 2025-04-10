package site.dogether.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.member.entity.Member;
import site.dogether.member.service.MemberService;
import site.dogether.notification.entity.NotificationTokenJpaEntity;
import site.dogether.notification.firebase.sender.SimpleFcmNotificationRequest;
import site.dogether.notification.repository.NotificationTokenJpaRepository;
import site.dogether.notification.sender.NotificationSender;
import site.dogether.notification.exception.InvalidNotificationTokenException;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationTokenJpaRepository notificationTokenJpaRepository;
    private final NotificationSender notificationSender;
    private final MemberService memberService;

    @Transactional
    public void sendNotification(
        final Long recipientId,
        final String title,
        final String body,
        final String type
    ) {
        notificationTokenJpaRepository.findAllByMember_Id(recipientId).forEach(
            notificationToken -> sndNotification(notificationToken, title, body, type));
    }

    private void sndNotification(final NotificationTokenJpaEntity notificationToken, String title, String body, String type) {
        try {
            final SimpleFcmNotificationRequest simpleFcmNotificationRequest = new SimpleFcmNotificationRequest(notificationToken.getValue(), title, body, type);
            notificationSender.send(simpleFcmNotificationRequest);
        } catch (final InvalidNotificationTokenException e) {
            notificationTokenJpaRepository.deleteAllByValue(notificationToken.getValue());
            log.info("유효하지 않은 토큰 제거 - {}", notificationToken.getValue());
        }
    }

    @Transactional
    public void saveNotificationToken(final Long memberId, final String notificationToken) {
        validateNotificationTokenIsNullOrEmpty(notificationToken);

        final Member member = memberService.getMember(memberId);
        if (notificationTokenJpaRepository.existsByMemberAndValue(member, notificationToken)) {
            return;
        }

        final NotificationTokenJpaEntity notificationTokenJpaEntity = new NotificationTokenJpaEntity(member, notificationToken);
        notificationTokenJpaRepository.save(notificationTokenJpaEntity);
        log.info("푸시 알림 토큰 저장 - {}", notificationToken);
    }

    private void validateNotificationTokenIsNullOrEmpty(final String token) {
        if (token == null || token.isEmpty()) {
            log.info("입력된 알림 토큰 값이 null 혹은 공백 - {}", token);
            throw new InvalidNotificationTokenException("알림 토큰 값은 공백일 수 없습니다.");
        }
    }

    @Transactional
    public void deleteNotificationToken(final Long memberId, final String notificationToken) {
        validateNotificationTokenIsNullOrEmpty(notificationToken);

        final Member member = memberService.getMember(memberId);
        notificationTokenJpaRepository.findByMemberAndValue(member, notificationToken)
                .ifPresent(notificationTokenJpaEntity -> {
                    notificationTokenJpaRepository.delete(notificationTokenJpaEntity);
                    log.info("푸시 알림 토큰 제거 - {}", notificationToken);});
    }
}
