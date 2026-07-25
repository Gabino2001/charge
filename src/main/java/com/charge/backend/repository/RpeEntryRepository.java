package com.charge.backend.repository;

import com.charge.backend.domain.RpeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RpeEntryRepository extends JpaRepository<RpeEntry, Long> {
    Optional<RpeEntry> findByPlayerIdAndEntryDate(Long playerId, LocalDate entryDate);
    List<RpeEntry> findByPlayerIdOrderByEntryDateDesc(Long playerId);
    boolean existsByPlayerIdAndEntryDate(Long playerId, LocalDate entryDate);
    void deleteByPlayerId(Long playerId);
}
