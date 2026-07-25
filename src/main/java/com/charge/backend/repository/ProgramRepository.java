package com.charge.backend.repository;

import com.charge.backend.domain.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramRepository extends JpaRepository<Program, Long> {
    List<Program> findByCoachIdOrderByCreatedAtDesc(Long coachId);
}
