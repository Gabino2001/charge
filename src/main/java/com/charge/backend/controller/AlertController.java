package com.charge.backend.controller;

import com.charge.backend.dto.AlertDtos.PlayerAlert;
import com.charge.backend.security.CurrentUser;
import com.charge.backend.service.AlertService;
import com.charge.backend.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('COACH')")
public class AlertController {

    private final AlertService alertService;
    private final PlayerService playerService;

    /** Alertes actives pour un joueur (fatigue prolongée, douleur répétée...). */
    @GetMapping("/api/players/{playerId}/alerts")
    public List<PlayerAlert> alertsForPlayer(@PathVariable Long playerId) {
        playerService.getPlayerOwnedByCoach(CurrentUser.id(), playerId);
        return alertService.computeAlerts(playerId);
    }
}
