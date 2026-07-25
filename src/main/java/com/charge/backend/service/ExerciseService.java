package com.charge.backend.service;

import com.charge.backend.domain.Exercise;
import com.charge.backend.domain.TrainingSession;
import com.charge.backend.domain.User;
import com.charge.backend.dto.ExerciseDtos.CreateExerciseRequest;
import com.charge.backend.dto.ExerciseDtos.ExerciseResponse;
import com.charge.backend.dto.ExerciseDtos.UpdateExerciseRequest;
import com.charge.backend.exception.ForbiddenOperationException;
import com.charge.backend.exception.ResourceNotFoundException;
import com.charge.backend.exception.WellnessRequiredException;
import com.charge.backend.repository.ExerciseRepository;
import com.charge.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final PlayerService playerService;
    private final WellnessService wellnessService;
    private final NotificationService notificationService;
    private final TrainingSessionService trainingSessionService;

    @Transactional
    public ExerciseResponse assign(Long coachId, Long playerId, CreateExerciseRequest request) {
        User player = playerService.getPlayerOwnedByCoach(coachId, playerId);
        User coach = userRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Préparateur introuvable : " + coachId));

        LocalDate date = request.scheduledFor() != null ? request.scheduledFor() : LocalDate.now();
        TrainingSession session = trainingSessionService.resolveOpenSession(playerId, date);

        int orderIndex = (int) exerciseRepository.findByPlayerIdAndArchivedFalseOrderByCreatedAtDesc(playerId).stream()
                .filter(e -> java.util.Objects.equals(e.getSessionId(), session.getId()))
                .filter(e -> java.util.Objects.equals(e.getBlockIndex(), request.blockIndex()))
                .count();

        Exercise exercise = Exercise.builder()
                .title(request.title())
                .sets(request.sets())
                .reps(request.reps())
                .videoUrl(request.videoUrl())
                .scheduledFor(request.scheduledFor())
                .recoveryTimeSeconds(request.recoveryTimeSeconds())
                .sessionType(request.sessionType())
                .blockIndex(request.blockIndex())
                .blockRecoveryTimeSeconds(request.blockRecoveryTimeSeconds())
                .percentRm(request.percentRm())
                .player(player)
                .assignedBy(coach)
                .sessionId(session.getId())
                .sessionNumber(session.getSessionNumber())
                .orderIndex(orderIndex)
                .build();
        exerciseRepository.save(exercise);
        return toResponse(exercise);
    }

    /** Le coach réordonne les exercices d'un même atelier (glisser un exercice avant/après un autre). */
    @Transactional
    public void reorder(Long coachId, com.charge.backend.dto.ExerciseDtos.ReorderExercisesRequest request) {
        int index = 0;
        for (Long exerciseId : request.orderedExerciseIds()) {
            Exercise exercise = exerciseRepository.findById(exerciseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Exercice introuvable : " + exerciseId));
            if (exercise.getPlayer().getCoach() == null || !exercise.getPlayer().getCoach().getId().equals(coachId)) {
                throw new ForbiddenOperationException("Cet exercice ne fait pas partie de votre effectif.");
            }
            exercise.setOrderIndex(index++);
            exerciseRepository.save(exercise);
        }
    }

    public List<ExerciseResponse> listForPlayer(Long coachId, Long playerId) {
        playerService.getPlayerOwnedByCoach(coachId, playerId);
        return exerciseRepository.findByPlayerIdAndArchivedFalseOrderByCreatedAtDesc(playerId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Liste des exercices du joueur connecté. Bloqué tant que le bien-être du jour n'est pas rempli. */
    public List<ExerciseResponse> listMine(Long playerId) {
        requireWellnessSubmitted(playerId);
        return exerciseRepository.findByPlayerIdAndArchivedFalseOrderByCreatedAtDesc(playerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ExerciseResponse toggleComplete(Long playerId, Long exerciseId) {
        requireWellnessSubmitted(playerId);

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercice introuvable : " + exerciseId));
        if (!exercise.getPlayer().getId().equals(playerId)) {
            throw new ForbiddenOperationException("Cet exercice n'est pas assigné à ce joueur.");
        }

        boolean nowDone = !exercise.isDone();
        exercise.setDone(nowDone);
        exercise.setCompletedAt(nowDone ? Instant.now() : null);
        if (!nowDone) {
            // Décocher un exercice invalide le ressenti déjà noté : il ne correspond plus à un exercice "fait".
            exercise.setExerciseRpe(null);
        }
        exerciseRepository.save(exercise);

        if (nowDone) {
            notificationService.notifyExerciseCompleted(exercise);
        }
        return toResponse(exercise);
    }

    /** Le joueur note son ressenti à l'effort juste après avoir terminé cet exercice précis. */
    @Transactional
    public ExerciseResponse submitExerciseRpe(Long playerId, Long exerciseId, com.charge.backend.dto.ExerciseDtos.SubmitExerciseRpeRequest request) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercice introuvable : " + exerciseId));
        if (!exercise.getPlayer().getId().equals(playerId)) {
            throw new ForbiddenOperationException("Cet exercice n'est pas assigné à ce joueur.");
        }
        exercise.setExerciseRpe(request.rpe());
        exercise.setLoadFeedback(request.loadFeedback());
        exercise.setLoadComment(request.loadComment());
        exerciseRepository.save(exercise);
        if ("TOO_HEAVY".equals(request.loadFeedback())) {
            notificationService.notifyLoadFeedback(exercise);
        }
        return toResponse(exercise);
    }

    /** Le préparateur retire un exercice de la séance d'un joueur (suppression logique : conserve l'historique). */
    @Transactional
    public void delete(Long coachId, Long exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercice introuvable : " + exerciseId));
        if (exercise.getPlayer().getCoach() == null || !exercise.getPlayer().getCoach().getId().equals(coachId)) {
            throw new ForbiddenOperationException("Cet exercice ne fait pas partie de votre effectif.");
        }
        exercise.setArchived(true);
        exerciseRepository.save(exercise);
    }

    /** Le préparateur corrige un exercice déjà envoyé (titre, séries, reps, vidéo, date). */
    @Transactional
    public ExerciseResponse update(Long coachId, Long exerciseId, UpdateExerciseRequest request) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercice introuvable : " + exerciseId));
        if (exercise.getPlayer().getCoach() == null || !exercise.getPlayer().getCoach().getId().equals(coachId)) {
            throw new ForbiddenOperationException("Cet exercice ne fait pas partie de votre effectif.");
        }
        exercise.setTitle(request.title());
        exercise.setSets(request.sets());
        exercise.setReps(request.reps());
        exercise.setVideoUrl(request.videoUrl());
        exercise.setRecoveryTimeSeconds(request.recoveryTimeSeconds());
        exercise.setSessionType(request.sessionType());
        exercise.setBlockIndex(request.blockIndex());
        exercise.setBlockRecoveryTimeSeconds(request.blockRecoveryTimeSeconds());
        exercise.setPercentRm(request.percentRm());
        if (request.scheduledFor() != null) {
            exercise.setScheduledFor(request.scheduledFor());
        }
        exerciseRepository.save(exercise);
        return toResponse(exercise);
    }

    private void requireWellnessSubmitted(Long playerId) {
        if (!wellnessService.hasSubmittedToday(playerId)) {
            throw new WellnessRequiredException(
                    "Le questionnaire de bien-être du jour doit être rempli avant d'accéder aux exercices.");
        }
    }

    private ExerciseResponse toResponse(Exercise e) {
        return new ExerciseResponse(
                e.getId(), e.getTitle(), e.getSets(), e.getReps(), e.getVideoUrl(), e.getScheduledFor(),
                e.isDone(), e.getPlayer().getId(), e.getCreatedAt(), e.getCompletedAt(),
                e.getRecoveryTimeSeconds(), e.getSessionType(), e.getBlockIndex(), e.getBlockRecoveryTimeSeconds(),
                e.getExerciseRpe(), e.getSessionId(), e.getSessionNumber(), e.getPercentRm(),
                e.getLoadFeedback(), e.getLoadComment(), e.getOrderIndex()
        );
    }
}
