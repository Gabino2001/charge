package com.charge.backend.controller;

import com.charge.backend.dto.FicheDtos.CreateFicheEntryRequest;
import com.charge.backend.dto.FicheDtos.FicheEntryResponse;
import com.charge.backend.dto.FicheDtos.UpdateFicheEntryRequest;
import com.charge.backend.security.CurrentUser;
import com.charge.backend.service.FicheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FicheController {

    private final FicheService ficheService;

    /** Le préparateur ajoute un test (poids x reps) : le 1RM et le tableau 1RM-20RM sont calculés automatiquement. */
    @PostMapping("/api/players/{playerId}/fiche")
    @PreAuthorize("hasRole('COACH')")
    public ResponseEntity<FicheEntryResponse> addEntry(@PathVariable Long playerId, @Valid @RequestBody CreateFicheEntryRequest request) {
        FicheEntryResponse response = ficheService.addEntry(CurrentUser.id(), playerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/fiche/{entryId}")
    @PreAuthorize("hasRole('COACH')")
    public FicheEntryResponse updateEntry(@PathVariable Long entryId, @Valid @RequestBody UpdateFicheEntryRequest request) {
        return ficheService.updateEntry(CurrentUser.id(), entryId, request);
    }

    @GetMapping("/api/players/{playerId}/fiche")
    @PreAuthorize("hasRole('COACH')")
    public List<FicheEntryResponse> listForPlayer(@PathVariable Long playerId) {
        return ficheService.listForPlayer(CurrentUser.id(), playerId);
    }

    @GetMapping("/api/fiche/mine")
    @PreAuthorize("hasRole('PLAYER')")
    public List<FicheEntryResponse> listMine() {
        return ficheService.listMine(CurrentUser.id());
    }

    /** Historique du 1RM pour un exercice donné (courbe de progression). */
    @GetMapping("/api/players/{playerId}/fiche/history")
    @PreAuthorize("hasRole('COACH')")
    public List<com.charge.backend.dto.TrendDtos.TrendPoint> history(
            @PathVariable Long playerId, @RequestParam String exerciseName) {
        return ficheService.oneRepMaxHistory(CurrentUser.id(), playerId, exerciseName);
    }
}
