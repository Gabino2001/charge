package com.charge.backend.repository;

import com.charge.backend.domain.Injury;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InjuryRepository extends JpaRepository<Injury, Long> {
    List<Injury> findByPlayerIdOrderByStartDateDesc(Long playerId);
    void deleteByPlayerId(Long playerId);
}
