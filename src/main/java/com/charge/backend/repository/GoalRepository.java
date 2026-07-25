package com.charge.backend.repository;

import com.charge.backend.domain.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByPlayerIdOrderByCreatedAtDesc(Long playerId);
    void deleteByPlayerId(Long playerId);
}
