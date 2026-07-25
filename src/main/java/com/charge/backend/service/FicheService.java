package com.charge.backend.service;

import com.charge.backend.domain.FicheEntry;
import com.charge.backend.domain.FicheHistoryLog;
import com.charge.backend.domain.User;
import com.charge.backend.dto.FicheDtos.CreateFicheEntryRequest;
import com.charge.backend.dto.FicheDtos.FicheEntryResponse;
import com.charge.backend.dto.FicheDtos.UpdateFicheEntryRequest;
import com.charge.backend.dto.TrendDtos.TrendPoint;
import com.charge.backend.exception.ResourceNotFoundException;
import com.charge.backend.repository.FicheEntryRepository;
import com.charge.backend.repository.FicheHistoryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FicheService {

    private final FicheEntryRepository ficheEntryRepository;
    private final FicheHistoryLogRepository ficheHistoryLogRepository;
    private final PlayerService playerService;

    @Transactional
    public FicheEntryResponse addEntry(Long coachId, Long playerId, CreateFicheEntryRequest request) {
        User player = playerService.getPlayerOwnedByCoach(coachId, playerId);
        FicheEntry entry = FicheEntry.builder()
                .exerciseName(request.exerciseName())
                .weight(request.weight())
                .reps(request.reps())
                .player(player)
                .build();
        ficheEntryRepository.save(entry);
        logHistory(player, entry);
        return toResponse(entry);
    }

    @Transactional
    public FicheEntryResponse updateEntry(Long coachId, Long entryId, UpdateFicheEntryRequest request) {
        FicheEntry entry = ficheEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Ligne de fiche introuvable : " + entryId));
        // Vérifie que le joueur propriétaire de la fiche appartient bien au préparateur connecté.
        User player = playerService.getPlayerOwnedByCoach(coachId, entry.getPlayer().getId());

        entry.setWeight(request.weight());
        entry.setReps(request.reps());
        ficheEntryRepository.save(entry);
        logHistory(player, entry);
        return toResponse(entry);
    }

    public List<FicheEntryResponse> listForPlayer(Long coachId, Long playerId) {
        playerService.getPlayerOwnedByCoach(coachId, playerId);
        return ficheEntryRepository.findByPlayerIdOrderByCreatedAtAsc(playerId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<FicheEntryResponse> listMine(Long playerId) {
        return ficheEntryRepository.findByPlayerIdOrderByCreatedAtAsc(playerId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Historique du 1RM pour un exercice donné, utilisé pour tracer une courbe de progression. */
    public List<TrendPoint> oneRepMaxHistory(Long coachId, Long playerId, String exerciseName) {
        playerService.getPlayerOwnedByCoach(coachId, playerId);
        return ficheHistoryLogRepository.findByPlayerIdAndExerciseNameOrderByRecordedAtAsc(playerId, exerciseName).stream()
                .map(h -> new TrendPoint(h.getRecordedAt().atZone(ZoneOffset.UTC).toLocalDate(), h.getOneRepMax()))
                .toList();
    }

    private void logHistory(User player, FicheEntry entry) {
        double oneRM = RMCalculator.oneRepMax(entry.getWeight(), entry.getReps());
        ficheHistoryLogRepository.save(
                FicheHistoryLog.builder()
                        .player(player)
                        .exerciseName(entry.getExerciseName())
                        .oneRepMax(oneRM)
                        .build()
        );
    }

    private FicheEntryResponse toResponse(FicheEntry entry) {
        double oneRM = RMCalculator.oneRepMax(entry.getWeight(), entry.getReps());
        return new FicheEntryResponse(
                entry.getId(), entry.getExerciseName(), entry.getWeight(), entry.getReps(),
                oneRM, RMCalculator.rmTable(oneRM), entry.getUpdatedAt()
        );
    }
}
