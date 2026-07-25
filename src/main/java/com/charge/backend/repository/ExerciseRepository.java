package com.charge.backend.repository;

import com.charge.backend.domain.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByPlayerIdOrderByCreatedAtDesc(Long playerId);
    List<Exercise> findByPlayerIdAndArchivedFalseOrderByCreatedAtDesc(Long playerId);
    void deleteByPlayerId(Long playerId);

    /** Joueurs distincts ayant déjà reçu ce programme, pour permettre au préparateur de le "renvoyer". */
    @Query("SELECT DISTINCT e.player.id FROM Exercise e WHERE e.sourceProgramId = :programId")
    List<Long> findDistinctPlayerIdsBySourceProgramId(Long programId);
}
