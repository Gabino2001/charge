package com.charge.backend.repository;

import com.charge.backend.domain.FicheHistoryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FicheHistoryLogRepository extends JpaRepository<FicheHistoryLog, Long> {
    List<FicheHistoryLog> findByPlayerIdAndExerciseNameOrderByRecordedAtAsc(Long playerId, String exerciseName);
    List<FicheHistoryLog> findDistinctByPlayerId(Long playerId);
    void deleteByPlayerId(Long playerId);
}
