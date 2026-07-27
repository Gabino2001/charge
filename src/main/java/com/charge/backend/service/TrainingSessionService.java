package com.charge.backend.service;

import com.charge.backend.domain.TrainingSession;
import com.charge.backend.domain.TrainingSessionStatus;
import com.charge.backend.dto.TrainingSessionDtos.SessionResponse;
import com.charge.backend.dto.TrainingSessionDtos.SubmitSessionRpeRequest;
import com.charge.backend.exception.ForbiddenOperationException;
import com.charge.backend.exception.ResourceNotFoundException;
import com.charge.backend.repository.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Gère les séances d'entraînement : un joueur peut avoir plusieurs séances distinctes le même jour
 * (le préparateur peut envoyer un nouveau lot d'exercices après que la première séance a été validée).
 * Chaque séance a son propre ressenti global (RPE), au lieu d'un RPE unique par jour.
 */
@Service
@RequiredArgsConstructor
public class TrainingSessionService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final PlayerService playerService;

    /**
     * Retourne la séance "ouverte" du jour pour ce joueur (celle à laquelle rattacher un nouvel exercice) :
     * réutilise la dernière séance du jour si elle n'est pas encore terminée, sinon en crée une nouvelle.
     */
    @Transactional
    public TrainingSession resolveOpenSession(Long playerId, LocalDate date) {
        List<TrainingSession> sessionsToday = trainingSessionRepository
                .findByPlayerIdAndSessionDateOrderBySessionNumberDesc(playerId, date);
        TrainingSession latest = sessionsToday.isEmpty() ? null : sessionsToday.get(0);

        if (latest != null && latest.getStatus() == TrainingSessionStatus.IN_PROGRESS) {
            return latest;
        }

        TrainingSession created = TrainingSession.builder()
                .playerId(playerId)
                .sessionDate(date)
                .sessionNumber(latest == null ? 1 : latest.getSessionNumber() + 1)
                .status(TrainingSessionStatus.IN_PROGRESS)
                .build();
        return trainingSessionRepository.save(created);
    }

    public List<SessionResponse> listToday(Long playerId) {
        return trainingSessionRepository
                .findByPlayerIdAndSessionDateOrderBySessionNumberDesc(playerId, LocalDate.now())
                .stream()
                .sorted((a, b) -> a.getSessionNumber().compareTo(b.getSessionNumber()))
                .map(this::toResponse)
                .toList();
    }

    public List<SessionResponse> history(Long playerId) {
        return trainingSessionRepository.findByPlayerIdOrderBySessionDateDescSessionNumberDesc(playerId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SessionResponse> historyForPlayer(Long coachId, Long playerId) {
        playerService.getPlayerOwnedByCoach(coachId, playerId);
        return history(playerId);
    }

    /** Ratio de charge aiguë/chronique du joueur (indicateur de risque de blessure lié à la charge). */
    public com.charge.backend.dto.TrainingSessionDtos.AcwrResponse acwrForPlayer(Long coachId, Long playerId) {
        playerService.getPlayerOwnedByCoach(coachId, playerId);
        List<TrainingSession> recent = trainingSessionRepository
                .findByPlayerIdAndSessionDateGreaterThanEqual(playerId, LocalDate.now().minusDays(27));
        return AcwrCalculator.compute(recent);
    }

    /** Courbe d'évolution du ratio ACWR sur les 60 derniers jours, pour visualiser la tendance de charge du joueur. */
    public List<com.charge.backend.dto.TrainingSessionDtos.AcwrHistoryPoint> acwrHistoryForPlayer(Long coachId, Long playerId) {
        playerService.getPlayerOwnedByCoach(coachId, playerId);
        int daysToShow = 60;
        // Il faut l'historique de charge sur daysToShow + 27 jours pour pouvoir calculer
        // la fenêtre chronique (28j) du tout premier point affiché.
        List<TrainingSession> sessions = trainingSessionRepository
                .findByPlayerIdAndSessionDateGreaterThanEqual(playerId, LocalDate.now().minusDays(daysToShow + 27));
        return AcwrCalculator.computeHistory(sessions, daysToShow);
    }

    @Transactional
    public SessionResponse submitRpe(Long playerId, Long sessionId, SubmitSessionRpeRequest request) {
        TrainingSession session = getOwned(playerId, sessionId);
        if (session.getStatus() == TrainingSessionStatus.COMPLETED) {
            throw new IllegalArgumentException("Le ressenti de cette séance a déjà été envoyé. Utilise la correction pour le modifier.");
        }
        session.setRpe(request.rpe());
        session.setDurationMinutes(request.durationMinutes());
        session.setComment(request.comment());
        session.setStatus(TrainingSessionStatus.COMPLETED);
        session.setCompletedAt(Instant.now());
        trainingSessionRepository.save(session);
        return toResponse(session);
    }

    @Transactional
    public SessionResponse updateRpe(Long playerId, Long sessionId, SubmitSessionRpeRequest request) {
        TrainingSession session = getOwned(playerId, sessionId);
        session.setRpe(request.rpe());
        session.setDurationMinutes(request.durationMinutes());
        session.setComment(request.comment());
        trainingSessionRepository.save(session);
        return toResponse(session);
    }

    private TrainingSession getOwned(Long playerId, Long sessionId) {
        TrainingSession session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Séance introuvable : " + sessionId));
        if (!session.getPlayerId().equals(playerId)) {
            throw new ForbiddenOperationException("Cette séance ne t'appartient pas.");
        }
        return session;
    }

    private SessionResponse toResponse(TrainingSession s) {
        return new SessionResponse(
                s.getId(), s.getSessionDate(), s.getSessionNumber(), s.getStatus().name(),
                s.getRpe(), s.getDurationMinutes(), s.getTrainingLoad(), s.getComment(), s.getCompletedAt()
        );
    }
}
