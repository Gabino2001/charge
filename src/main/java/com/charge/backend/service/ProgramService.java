package com.charge.backend.service;

import com.charge.backend.domain.Exercise;
import com.charge.backend.domain.Program;
import com.charge.backend.domain.ProgramBlock;
import com.charge.backend.domain.ProgramExercise;
import com.charge.backend.domain.TrainingSession;
import com.charge.backend.domain.User;
import com.charge.backend.dto.ProgramDtos.*;
import com.charge.backend.exception.ForbiddenOperationException;
import com.charge.backend.exception.ResourceNotFoundException;
import com.charge.backend.repository.ExerciseRepository;
import com.charge.backend.repository.ProgramRepository;
import com.charge.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramRepository programRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final PlayerService playerService;
    private final TrainingSessionService trainingSessionService;

    @Transactional
    public ProgramResponse create(Long coachId, CreateProgramRequest request) {
        User coach = userRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Préparateur introuvable : " + coachId));

        Program program = Program.builder()
                .name(request.name())
                .description(request.description())
                .sessionType(request.sessionType())
                .coach(coach)
                .build();

        List<ProgramBlock> blocks = new ArrayList<>();
        int blockOrder = 0;
        for (ProgramBlockRequest blockReq : request.blocks()) {
            ProgramBlock block = ProgramBlock.builder()
                    .program(program)
                    .recoveryTimeSeconds(blockReq.recoveryTimeSeconds())
                    .orderIndex(blockOrder++)
                    .build();

            List<ProgramExercise> exercises = new ArrayList<>();
            int exOrder = 0;
            for (ProgramExerciseRequest exReq : blockReq.exercises()) {
                exercises.add(ProgramExercise.builder()
                        .block(block)
                        .title(exReq.title())
                        .sets(exReq.sets())
                        .reps(exReq.reps())
                        .videoUrl(exReq.videoUrl())
                        .recoveryTimeSeconds(exReq.recoveryTimeSeconds())
                        .percentRm(exReq.percentRm())
                        .orderIndex(exOrder++)
                        .build());
            }
            block.setExercises(exercises);
            blocks.add(block);
        }
        program.setBlocks(blocks);

        programRepository.save(program);
        return toResponse(program);
    }

    /** Le préparateur modifie un programme existant (nom, description, type de séance, ateliers/exercices). */
    @Transactional
    public ProgramResponse update(Long coachId, Long programId, CreateProgramRequest request) {
        Program program = getOwnedProgram(coachId, programId);

        program.setName(request.name());
        program.setDescription(request.description());
        program.setSessionType(request.sessionType());

        // Les blocs/exercices existants sont remplacés par la nouvelle liste (orphanRemoval supprime les anciens).
        program.getBlocks().clear();

        List<ProgramBlock> blocks = new ArrayList<>();
        int blockOrder = 0;
        for (ProgramBlockRequest blockReq : request.blocks()) {
            ProgramBlock block = ProgramBlock.builder()
                    .program(program)
                    .recoveryTimeSeconds(blockReq.recoveryTimeSeconds())
                    .orderIndex(blockOrder++)
                    .build();

            List<ProgramExercise> exercises = new ArrayList<>();
            int exOrder = 0;
            for (ProgramExerciseRequest exReq : blockReq.exercises()) {
                exercises.add(ProgramExercise.builder()
                        .block(block)
                        .title(exReq.title())
                        .sets(exReq.sets())
                        .reps(exReq.reps())
                        .videoUrl(exReq.videoUrl())
                        .recoveryTimeSeconds(exReq.recoveryTimeSeconds())
                        .percentRm(exReq.percentRm())
                        .orderIndex(exOrder++)
                        .build());
            }
            block.setExercises(exercises);
            blocks.add(block);
        }
        program.getBlocks().addAll(blocks);

        programRepository.save(program);
        return toResponse(program);
    }

    public List<ProgramResponse> list(Long coachId) {
        return programRepository.findByCoachIdOrderByCreatedAtDesc(coachId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long coachId, Long programId) {
        Program program = getOwnedProgram(coachId, programId);
        programRepository.delete(program);
    }

    /** Assigne tous les ateliers/exercices du programme à chaque joueur listé (doit appartenir à l'effectif du préparateur). */
    @Transactional
    public AssignProgramResponse assign(Long coachId, Long programId, AssignProgramRequest request) {
        Program program = getOwnedProgram(coachId, programId);
        User coach = program.getCoach();
        int totalExercises = assignToPlayers(coach, coachId, program, request.playerIds());
        return new AssignProgramResponse(request.playerIds().size(), totalExercises);
    }

    /**
     * Renvoie le programme (dans sa version actuelle, après modification) à tous les joueurs qui l'avaient déjà reçu.
     * Pratique quand le préparateur corrige un programme et veut pousser la mise à jour sans réassigner un par un.
     */
    @Transactional
    public AssignProgramResponse resend(Long coachId, Long programId) {
        Program program = getOwnedProgram(coachId, programId);
        User coach = program.getCoach();
        List<Long> playerIds = exerciseRepository.findDistinctPlayerIdsBySourceProgramId(programId);
        if (playerIds.isEmpty()) {
            throw new IllegalArgumentException("Ce programme n'a encore été assigné à aucun joueur.");
        }
        int totalExercises = assignToPlayers(coach, coachId, program, playerIds);
        return new AssignProgramResponse(playerIds.size(), totalExercises);
    }

    private int assignToPlayers(User coach, Long coachId, Program program, List<Long> playerIds) {
        int totalExercises = program.getBlocks().stream().mapToInt(b -> b.getExercises().size()).sum();

        for (Long playerId : playerIds) {
            User player = playerService.getPlayerOwnedByCoach(coachId, playerId);
            TrainingSession session = trainingSessionService.resolveOpenSession(playerId, LocalDate.now());
            int blockIndex = 1;
            for (ProgramBlock block : program.getBlocks()) {
                for (ProgramExercise pe : block.getExercises()) {
                    Exercise exercise = Exercise.builder()
                            .title(pe.getTitle())
                            .sets(pe.getSets())
                            .reps(pe.getReps())
                            .videoUrl(pe.getVideoUrl())
                            .recoveryTimeSeconds(pe.getRecoveryTimeSeconds())
                            .percentRm(pe.getPercentRm())
                            .sessionType(program.getSessionType())
                            .blockIndex(blockIndex)
                            .blockRecoveryTimeSeconds(block.getRecoveryTimeSeconds())
                            .player(player)
                            .assignedBy(coach)
                            .sourceProgramId(program.getId())
                            .sessionId(session.getId())
                            .sessionNumber(session.getSessionNumber())
                            .build();
                    exerciseRepository.save(exercise);
                }
                blockIndex++;
            }
        }
        return totalExercises;
    }

    private Program getOwnedProgram(Long coachId, Long programId) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Programme introuvable : " + programId));
        if (!program.getCoach().getId().equals(coachId)) {
            throw new ForbiddenOperationException("Ce programme ne vous appartient pas.");
        }
        return program;
    }

    private ProgramResponse toResponse(Program program) {
        List<ProgramBlockResponse> blocks = program.getBlocks().stream()
                .map(b -> new ProgramBlockResponse(
                        b.getId(),
                        b.getRecoveryTimeSeconds(),
                        b.getExercises().stream()
                                .map(e -> new ProgramExerciseResponse(e.getId(), e.getTitle(), e.getSets(), e.getReps(), e.getVideoUrl(), e.getRecoveryTimeSeconds(), e.getPercentRm()))
                                .toList()
                ))
                .toList();
        return new ProgramResponse(program.getId(), program.getName(), program.getDescription(), program.getSessionType(), blocks);
    }
}
