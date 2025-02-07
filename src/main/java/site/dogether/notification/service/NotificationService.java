package site.dogether.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.dogether.member.infrastructure.entity.MemberJpaEntity;
import site.dogether.member.service.MemberService;
import site.dogether.notification.infrastructure.entity.NotificationTokenJpaEntity;
import site.dogether.notification.infrastructure.firebase.sender.SimpleFcmNotificationRequest;
import site.dogether.notification.infrastructure.repository.NotificationTokenJpaRepository;
import site.dogether.notification.sender.NotificationSender;
import site.dogether.notification.service.exception.InvalidNotificationTokenException;

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
        final String body
    ) {
        notificationTokenJpaRepository.findAllByMember_Id(recipientId).forEach(
            notificationToken -> sndNotification(notificationToken, title, body));
    }

    private void sndNotification(final NotificationTokenJpaEntity notificationToken, String title, String body) {
        try {
            final SimpleFcmNotificationRequest simpleFcmNotificationRequest = new SimpleFcmNotificationRequest(notificationToken.getValue(), title, body);
            notificationSender.send(simpleFcmNotificationRequest);
        } catch (final InvalidNotificationTokenException e) {
            notificationTokenJpaRepository.deleteAllByValue(notificationToken.getValue());
            log.info("유효하지 않은 토큰 제거 - {}", notificationToken.getValue());
        }
    }

    @Transactional
    public void saveNotificationToken(final String authenticationToken, final String notificationToken) {
        validateNotificationTokenIsNullOrEmpty(notificationToken);

        final MemberJpaEntity memberJpaEntity = memberService.findMemberEntityByAuthenticationToken(authenticationToken);
        if (notificationTokenJpaRepository.existsByMemberAndValue(memberJpaEntity, notificationToken)) {
            return;
        }

        final NotificationTokenJpaEntity notificationTokenJpaEntity = new NotificationTokenJpaEntity(memberJpaEntity, notificationToken);
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
    public void deleteNotificationToken(final String authenticationToken, final String notificationToken) {
        validateNotificationTokenIsNullOrEmpty(notificationToken);

        final MemberJpaEntity memberJpaEntity = memberService.findMemberEntityByAuthenticationToken(authenticationToken);
        notificationTokenJpaRepository.findByMemberAndValue(memberJpaEntity, notificationToken)
                .ifPresent(notificationTokenJpaEntity -> {
                    notificationTokenJpaRepository.delete(notificationTokenJpaEntity);
                    log.info("푸시 알림 토큰 제거 - {}", notificationToken);});
    }
}
