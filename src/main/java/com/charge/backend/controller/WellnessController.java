package com.charge.backend.controller;

import com.charge.backend.dto.WellnessDtos.SubmitWellnessRequest;
import com.charge.backend.dto.WellnessDtos.WellnessResponse;
import com.charge.backend.dto.WellnessDtos.WellnessStatus;
import com.charge.backend.security.CurrentUser;
import com.charge.backend.service.PlayerService;
import com.charge.backend.service.WellnessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WellnessController {

    private final WellnessService wellnessService;
    private final PlayerService playerService;

    /** Le joueur remplit son questionnaire du jour (une seule fois par jour). */
    @PostMapping("/api/wellness")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<WellnessResponse> submit(@Valid @RequestBody SubmitWellnessRequest request) {
        WellnessResponse response = wellnessService.submitToday(CurrentUser.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Le joueur vérifie s'il a déjà rempli le questionnaire aujourd'hui (utilisé pour bloquer l'accès aux exercices). */
    @GetMapping("/api/wellness/today")
    @PreAuthorize("hasRole('PLAYER')")
    public WellnessStatus todayStatus() {
        return wellnessService.getTodayStatus(CurrentUser.id());
    }

    /** Le préparateur consulte l'historique de bien-être d'un joueur de son effectif. */
    @GetMapping("/api/players/{playerId}/wellness")
    @PreAuthorize("hasRole('COACH')")
    public List<WellnessResponse> historyForPlayer(@PathVariable Long playerId) {
        playerService.getPlayerOwnedByCoach(CurrentUser.id(), playerId);
        return wellnessService.history(playerId);
    }
}
