package com.charge.backend.controller;

import com.charge.backend.dto.TrainingSessionDtos.SessionResponse;
import com.charge.backend.dto.TrainingSessionDtos.SubmitSessionRpeRequest;
import com.charge.backend.security.CurrentUser;
import com.charge.backend.service.TrainingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TrainingSessionController {

    private final TrainingSessionService trainingSessionService;

    /** Les séances du joueur connecté pour aujourd'hui (il peut y en avoir plusieurs). */
    @GetMapping("/api/sessions/mine/today")
    @PreAuthorize("hasRole('PLAYER')")
    public List<SessionResponse> today() {
        return trainingSessionService.listToday(CurrentUser.id());
    }

    /** L'historique complet des séances du joueur connecté (pour consulter ses séances passées). */
    @GetMapping("/api/sessions/mine")
    @PreAuthorize("hasRole('PLAYER')")
    public List<SessionResponse> mine() {
        return trainingSessionService.history(CurrentUser.id());
    }

    /** Le joueur note son ressenti global une fois toute la séance terminée. */
    @PostMapping("/api/sessions/{sessionId}/rpe")
    @PreAuthorize("hasRole('PLAYER')")
    public SessionResponse submitRpe(@PathVariable Long sessionId, @Valid @RequestBody SubmitSessionRpeRequest request) {
        return trainingSessionService.submitRpe(CurrentUser.id(), sessionId, request);
    }

    /** Le joueur corrige le ressenti déjà envoyé pour cette séance. */
    @PutMapping("/api/sessions/{sessionId}/rpe")
    @PreAuthorize("hasRole('PLAYER')")
    public SessionResponse updateRpe(@PathVariable Long sessionId, @Valid @RequestBody SubmitSessionRpeRequest request) {
        return trainingSessionService.updateRpe(CurrentUser.id(), sessionId, request);
    }

    /** Le préparateur consulte l'historique des séances d'un joueur de son effectif (pour les courbes de charge). */
    @GetMapping("/api/players/{playerId}/sessions")
    @PreAuthorize("hasRole('COACH')")
    public List<SessionResponse> historyForPlayer(@PathVariable Long playerId) {
        return trainingSessionService.historyForPlayer(CurrentUser.id(), playerId);
    }

    /** Ratio de charge aiguë/chronique (risque de blessure lié à la charge d'entraînement). */
    @GetMapping("/api/players/{playerId}/acwr")
    @PreAuthorize("hasRole('COACH')")
    public com.charge.backend.dto.TrainingSessionDtos.AcwrResponse acwr(@PathVariable Long playerId) {
        return trainingSessionService.acwrForPlayer(CurrentUser.id(), playerId);
    }

    /** Courbe d'évolution du ratio ACWR sur les 60 derniers jours (pour visualiser la tendance de charge). */
    @GetMapping("/api/players/{playerId}/acwr/history")
    @PreAuthorize("hasRole('COACH')")
    public List<com.charge.backend.dto.TrainingSessionDtos.AcwrHistoryPoint> acwrHistory(@PathVariable Long playerId) {
        return trainingSessionService.acwrHistoryForPlayer(CurrentUser.id(), playerId);
    }
}
