package com.charge.backend.controller;

import com.charge.backend.dto.ExerciseDtos.CreateExerciseRequest;
import com.charge.backend.dto.ExerciseDtos.ExerciseResponse;
import com.charge.backend.security.CurrentUser;
import com.charge.backend.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    /** Le préparateur assigne un exercice à un joueur de son effectif. */
    @PostMapping("/api/players/{playerId}/exercises")
    @PreAuthorize("hasRole('COACH')")
    public ResponseEntity<ExerciseResponse> assign(@PathVariable Long playerId, @Valid @RequestBody CreateExerciseRequest request) {
        ExerciseResponse response = exerciseService.assign(CurrentUser.id(), playerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Le préparateur consulte les exercices d'un joueur de son effectif. */
    @GetMapping("/api/players/{playerId}/exercises")
    @PreAuthorize("hasRole('COACH')")
    public List<ExerciseResponse> listForPlayer(@PathVariable Long playerId) {
        return exerciseService.listForPlayer(CurrentUser.id(), playerId);
    }

    /** Le joueur consulte ses propres exercices. Bloqué (HTTP 428) si le bien-être du jour n'est pas rempli. */
    @GetMapping("/api/exercises/mine")
    @PreAuthorize("hasRole('PLAYER')")
    public List<ExerciseResponse> listMine() {
        return exerciseService.listMine(CurrentUser.id());
    }

    /** Le joueur coche/décoche un exercice. Déclenche une notification au préparateur quand il passe à "terminé". */
    @PatchMapping("/api/exercises/{exerciseId}/complete")
    @PreAuthorize("hasRole('PLAYER')")
    public ExerciseResponse toggleComplete(@PathVariable Long exerciseId) {
        return exerciseService.toggleComplete(CurrentUser.id(), exerciseId);
    }

    /** Le préparateur corrige un exercice déjà envoyé. */
    @PutMapping("/api/exercises/{exerciseId}")
    @PreAuthorize("hasRole('COACH')")
    public ExerciseResponse update(@PathVariable Long exerciseId, @Valid @RequestBody com.charge.backend.dto.ExerciseDtos.UpdateExerciseRequest request) {
        return exerciseService.update(CurrentUser.id(), exerciseId, request);
    }

    /** Le préparateur supprime un exercice de la séance d'un joueur. */
    @DeleteMapping("/api/exercises/{exerciseId}")
    @PreAuthorize("hasRole('COACH')")
    public ResponseEntity<Void> delete(@PathVariable Long exerciseId) {
        exerciseService.delete(CurrentUser.id(), exerciseId);
        return ResponseEntity.noContent().build();
    }

    /** Le joueur note son ressenti à l'effort juste après avoir terminé cet exercice précis. */
    @PatchMapping("/api/exercises/{exerciseId}/rpe")
    @PreAuthorize("hasRole('PLAYER')")
    public ExerciseResponse submitExerciseRpe(
            @PathVariable Long exerciseId,
            @Valid @RequestBody com.charge.backend.dto.ExerciseDtos.SubmitExerciseRpeRequest request
    ) {
        return exerciseService.submitExerciseRpe(CurrentUser.id(), exerciseId, request);
    }

    /** Le préparateur réordonne les exercices d'un atelier. */
    @PatchMapping("/api/exercises/reorder")
    @PreAuthorize("hasRole('COACH')")
    public ResponseEntity<Void> reorder(@Valid @RequestBody com.charge.backend.dto.ExerciseDtos.ReorderExercisesRequest request) {
        exerciseService.reorder(CurrentUser.id(), request);
        return ResponseEntity.noContent().build();
    }
}
