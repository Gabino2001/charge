package com.charge.backend.repository;

import com.charge.backend.domain.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    List<TrainingSession> findByPlayerIdAndSessionDateOrderBySessionNumberDesc(Long playerId, LocalDate sessionDate);
    List<TrainingSession> findByPlayerIdOrderBySessionDateDescSessionNumberDesc(Long playerId);
    List<TrainingSession> findByPlayerIdAndSessionDateGreaterThanEqual(Long playerId, LocalDate from);
    void deleteByPlayerId(Long playerId);
}
