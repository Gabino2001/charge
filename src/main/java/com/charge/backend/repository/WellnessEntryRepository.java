package com.charge.backend.repository;

import com.charge.backend.domain.WellnessEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WellnessEntryRepository extends JpaRepository<WellnessEntry, Long> {
    Optional<WellnessEntry> findByPlayerIdAndEntryDate(Long playerId, LocalDate entryDate);
    List<WellnessEntry> findByPlayerIdOrderByEntryDateDesc(Long playerId);
    boolean existsByPlayerIdAndEntryDate(Long playerId, LocalDate entryDate);
    void deleteByPlayerId(Long playerId);
}
