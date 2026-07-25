package com.charge.backend.service;

import com.charge.backend.domain.FicheEntry;
import com.charge.backend.domain.Goal;
import com.charge.backend.domain.User;
import com.charge.backend.dto.GoalDtos.CreateGoalRequest;
import com.charge.backend.dto.GoalDtos.GoalResponse;
import com.charge.backend.exception.ForbiddenOperationException;
import com.charge.backend.exception.ResourceNotFoundException;
import com.charge.backend.repository.FicheEntryRepository;
import com.charge.backend.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final FicheEntryRepository ficheEntryRepository;
    private final PlayerService playerService;

    @Transactional
    public GoalResponse create(Long coachId, Long playerId, CreateGoalRequest request) {
        User player = playerService.getPlayerOwnedByCoach(coachId, playerId);
        Goal goal = Goal.builder()
                .player(player)
                .exerciseName(request.exerciseName())
                .targetOneRepMax(request.targetOneRepMax())
                .targetDate(request.targetDate())
                .build();
        goalRepository.save(goal);
        return toResponse(goal);
    }

    public List<GoalResponse> listForPlayer(Long coachId, Long playerId) {
        playerService.getPlayerOwnedByCoach(coachId, playerId);
        return goalRepository.findByPlayerIdOrderByCreatedAtDesc(playerId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<GoalResponse> listMine(Long playerId) {
        return goalRepository.findByPlayerIdOrderByCreatedAtDesc(playerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long coachId, Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Objectif introuvable : " + goalId));
        if (goal.getPlayer().getCoach() == null || !goal.getPlayer().getCoach().getId().equals(coachId)) {
            throw new ForbiddenOperationException("Cet objectif ne fait pas partie de votre effectif.");
        }
        goalRepository.delete(goal);
    }

    private GoalResponse toResponse(Goal goal) {
        Optional<FicheEntry> latestTest = ficheEntryRepository
                .findTopByPlayerIdAndExerciseNameOrderByCreatedAtDesc(goal.getPlayer().getId(), goal.getExerciseName());

        Double currentOneRM = latestTest.map(e -> RMCalculator.oneRepMax(e.getWeight(), e.getReps())).orElse(null);
        Integer progress = null;
        boolean achieved = false;
        if (currentOneRM != null && goal.getTargetOneRepMax() > 0) {
            progress = (int) Math.round(Math.min(100.0, (currentOneRM / goal.getTargetOneRepMax()) * 100));
            achieved = currentOneRM >= goal.getTargetOneRepMax();
        }
        return new GoalResponse(goal.getId(), goal.getExerciseName(), goal.getTargetOneRepMax(), goal.getTargetDate(), currentOneRM, progress, achieved);
    }
}
