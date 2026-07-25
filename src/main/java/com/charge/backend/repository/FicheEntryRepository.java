package com.charge.backend.repository;

import com.charge.backend.domain.FicheEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FicheEntryRepository extends JpaRepository<FicheEntry, Long> {
    List<FicheEntry> findByPlayerIdOrderByCreatedAtAsc(Long playerId);
    java.util.Optional<FicheEntry> findTopByPlayerIdAndExerciseNameOrderByCreatedAtDesc(Long playerId, String exerciseName);
    void deleteByPlayerId(Long playerId);
}
