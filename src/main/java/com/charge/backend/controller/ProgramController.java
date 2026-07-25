package com.charge.backend.controller;

import com.charge.backend.dto.ProgramDtos.*;
import com.charge.backend.security.CurrentUser;
import com.charge.backend.service.ProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COACH')")
public class ProgramController {

    private final ProgramService programService;

    @PostMapping
    public ResponseEntity<ProgramResponse> create(@Valid @RequestBody CreateProgramRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(programService.create(CurrentUser.id(), request));
    }

    @GetMapping
    public List<ProgramResponse> list() {
        return programService.list(CurrentUser.id());
    }

    /** Le préparateur modifie un programme existant (ateliers, exercices, type de séance...). */
    @PutMapping("/{programId}")
    public ProgramResponse update(@PathVariable Long programId, @Valid @RequestBody CreateProgramRequest request) {
        return programService.update(CurrentUser.id(), programId, request);
    }

    @DeleteMapping("/{programId}")
    public ResponseEntity<Void> delete(@PathVariable Long programId) {
        programService.delete(CurrentUser.id(), programId);
        return ResponseEntity.noContent().build();
    }

    /** Assigne tous les exercices du programme aux joueurs listés. */
    @PostMapping("/{programId}/assign")
    public AssignProgramResponse assign(@PathVariable Long programId, @Valid @RequestBody AssignProgramRequest request) {
        return programService.assign(CurrentUser.id(), programId, request);
    }

    /** Renvoie la version actuelle du programme à tous les joueurs qui l'avaient déjà reçu (après une modification). */
    @PostMapping("/{programId}/resend")
    public AssignProgramResponse resend(@PathVariable Long programId) {
        return programService.resend(CurrentUser.id(), programId);
    }
}
