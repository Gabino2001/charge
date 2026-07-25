package com.charge.backend.service;

import com.charge.backend.domain.Exercise;
import com.charge.backend.domain.Notification;
import com.charge.backend.dto.NotificationDtos.NotificationResponse;
import com.charge.backend.exception.ForbiddenOperationException;
import com.charge.backend.exception.ResourceNotFoundException;
import com.charge.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    // Le service d'envoi push (Firebase Cloud Messaging) est branché ici : voir PushNotificationSender.

    private final PushNotificationSender pushNotificationSender;

    @Transactional
    public void notifyExerciseCompleted(Exercise exercise) {
        String message = exercise.getPlayer().getFullName() + " a terminé « " + exercise.getTitle() + " »";
        Notification notification = Notification.builder()
                .message(message)
                .recipient(exercise.getAssignedBy())
                .relatedExercise(exercise)
                .build();
        notificationRepository.save(notification);
        pushNotificationSender.send(exercise.getAssignedBy(), "Exercice terminé", message);
    }

    /** Le joueur signale que la charge donnée était trop lourde : le coach est notifié pour pouvoir ajuster. */
    @Transactional
    public void notifyLoadFeedback(Exercise exercise) {
        String message = exercise.getPlayer().getFullName() + " signale une charge trop lourde sur « " + exercise.getTitle() + " »";
        Notification notification = Notification.builder()
                .message(message)
                .recipient(exercise.getAssignedBy())
                .relatedExercise(exercise)
                .build();
        notificationRepository.save(notification);
        pushNotificationSender.send(exercise.getAssignedBy(), "Charge trop lourde", message);
    }

    public List<NotificationResponse> listForCoach(Long coachId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(coachId).stream()
                .map(this::toResponse)
                .toList();
    }

    public long countUnread(Long coachId) {
        return notificationRepository.countByRecipientIdAndReadFalse(coachId);
    }

    @Transactional
    public NotificationResponse markRead(Long coachId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable : " + notificationId));
        if (!notification.getRecipient().getId().equals(coachId)) {
            throw new ForbiddenOperationException("Cette notification ne vous appartient pas.");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
        return toResponse(notification);
    }

    private NotificationResponse toResponse(Notification n) {
        Long relatedId = n.getRelatedExercise() != null ? n.getRelatedExercise().getId() : null;
        return new NotificationResponse(n.getId(), n.getMessage(), n.isRead(), relatedId, n.getCreatedAt());
    }
}
