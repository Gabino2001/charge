package com.charge.backend.service;

import com.charge.backend.domain.Exercise;
import com.charge.backend.domain.Role;
import com.charge.backend.domain.User;
import com.charge.backend.dto.PlayerDtos.CreatePlayerRequest;
import com.charge.backend.dto.PlayerDtos.PlayerResponse;
import com.charge.backend.exception.ForbiddenOperationException;
import com.charge.backend.exception.ResourceNotFoundException;
import com.charge.backend.repository.ExerciseRepository;
import com.charge.backend.repository.FicheEntryRepository;
import com.charge.backend.repository.FicheHistoryLogRepository;
import com.charge.backend.repository.GoalRepository;
import com.charge.backend.repository.InjuryRepository;
import com.charge.backend.repository.NotificationRepository;
import com.charge.backend.repository.RpeEntryRepository;
import com.charge.backend.repository.UserRepository;
import com.charge.backend.repository.WellnessEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final FicheEntryRepository ficheEntryRepository;
    private final FicheHistoryLogRepository ficheHistoryLogRepository;
    private final WellnessEntryRepository wellnessEntryRepository;
    private final RpeEntryRepository rpeEntryRepository;
    private final com.charge.backend.repository.TrainingSessionRepository trainingSessionRepository;
    private final InjuryRepository injuryRepository;
    private final GoalRepository goalRepository;
    private final NotificationRepository notificationRepository;
    private final AlertService alertService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PlayerResponse createPlayer(Long coachId, CreatePlayerRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email.");
        }
        User coach = getCoach(coachId);
        User player = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.PLAYER)
                .poste(request.poste())
                .coach(coach)
                .build();
        userRepository.save(player);
        return toResponse(player);
    }

    public List<PlayerResponse> listPlayers(Long coachId) {
        return userRepository.findByCoachIdAndRole(coachId, Role.PLAYER).stream()
                .map(this::toResponse)
                .toList();
    }

    public PlayerResponse getPlayer(Long coachId, Long playerId) {
        User player = getPlayerOwnedByCoach(coachId, playerId);
        return toResponse(player);
    }

    /** Supprime un joueur et toutes ses données (exercices, fiche, bien-être). Action irréversible. */
    @Transactional
    public void deletePlayer(Long coachId, Long playerId) {
        User player = getPlayerOwnedByCoach(coachId, playerId);

        List<Long> exerciseIds = exerciseRepository.findByPlayerIdOrderByCreatedAtDesc(playerId).stream()
                .map(Exercise::getId)
                .toList();
        if (!exerciseIds.isEmpty()) {
            notificationRepository.clearRelatedExercise(exerciseIds);
        }

        exerciseRepository.deleteByPlayerId(playerId);
        ficheEntryRepository.deleteByPlayerId(playerId);
        ficheHistoryLogRepository.deleteByPlayerId(playerId);
        wellnessEntryRepository.deleteByPlayerId(playerId);
        rpeEntryRepository.deleteByPlayerId(playerId);
        trainingSessionRepository.deleteByPlayerId(playerId);
        injuryRepository.deleteByPlayerId(playerId);
        goalRepository.deleteByPlayerId(playerId);
        userRepository.delete(player);
    }

    /** Vérifie que le joueur appartient bien au préparateur authentifié, et le retourne. */
    public User getPlayerOwnedByCoach(Long coachId, Long playerId) {
        User player = userRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Joueur introuvable : " + playerId));
        if (player.getRole() != Role.PLAYER || player.getCoach() == null || !player.getCoach().getId().equals(coachId)) {
            throw new ForbiddenOperationException("Ce joueur n'est pas rattaché à votre effectif.");
        }
        return player;
    }

    private User getCoach(Long coachId) {
        User coach = userRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Préparateur introuvable : " + coachId));
        if (coach.getRole() != Role.COACH) {
            throw new ForbiddenOperationException("Seul un préparateur physique peut gérer un effectif.");
        }
        return coach;
    }

    private PlayerResponse toResponse(User player) {
        List<Exercise> todayExercises = exerciseRepository.findByPlayerIdAndArchivedFalseOrderByCreatedAtDesc(player.getId())
                .stream()
                .filter(e -> LocalDate.now().equals(e.getScheduledFor()))
                .toList();
        long total = todayExercises.size();
        long done = todayExercises.stream().filter(Exercise::isDone).count();

        boolean wellnessToday = wellnessEntryRepository.existsByPlayerIdAndEntryDate(player.getId(), LocalDate.now());

        // Le RPE est maintenant par séance (une même journée peut en avoir plusieurs) : "rempli aujourd'hui"
        // veut dire qu'au moins une séance du jour a été validée avec son ressenti envoyé.
        boolean rpeToday = trainingSessionRepository
                .findByPlayerIdAndSessionDateOrderBySessionNumberDesc(player.getId(), LocalDate.now())
                .stream()
                .anyMatch(s -> s.getStatus() == com.charge.backend.domain.TrainingSessionStatus.COMPLETED);

        boolean alerts = alertService.hasActiveAlerts(player.getId());

        List<com.charge.backend.domain.TrainingSession> recentSessions = trainingSessionRepository
                .findByPlayerIdAndSessionDateGreaterThanEqual(player.getId(), LocalDate.now().minusDays(27));
        com.charge.backend.dto.TrainingSessionDtos.AcwrResponse acwr = AcwrCalculator.compute(recentSessions);

        return new PlayerResponse(
                player.getId(), player.getFullName(), player.getPoste(), player.getInitials(), done, total,
                wellnessToday, rpeToday, alerts, acwr.ratio(), acwr.zone()
        );
    }
}
