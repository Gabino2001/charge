package com.charge.backend.repository;

import com.charge.backend.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    long countByRecipientIdAndReadFalse(Long recipientId);

    /** Détache les notifications des exercices sur le point d'être supprimés (garde l'historique du message). */
    @Modifying
    @Query("UPDATE Notification n SET n.relatedExercise = null WHERE n.relatedExercise.id IN :exerciseIds")
    void clearRelatedExercise(List<Long> exerciseIds);
}
