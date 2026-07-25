package com.charge.backend.service;

import com.charge.backend.domain.WellnessEntry;
import com.charge.backend.dto.AlertDtos.PlayerAlert;
import com.charge.backend.repository.WellnessEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calcule des alertes simples à partir de l'historique de bien-être récent d'un joueur.
 * Calcul "à la demande" (pas de tâche planifiée) : suffisant tant que le volume de données
 * reste faible ; à revoir avec un job planifié si l'effectif grossit beaucoup.
 */
@Service
@RequiredArgsConstructor
public class AlertService {

    private final WellnessEntryRepository wellnessEntryRepository;

    private static final int WINDOW = 3;
    private static final double FATIGUE_THRESHOLD = 2.4;

    public List<PlayerAlert> computeAlerts(Long playerId) {
        List<WellnessEntry> recent = wellnessEntryRepository.findByPlayerIdOrderByEntryDateDesc(playerId);
        List<WellnessEntry> lastN = recent.stream().limit(WINDOW).toList();
        List<PlayerAlert> alerts = new ArrayList<>();

        if (lastN.size() == WINDOW && lastN.stream().allMatch(e -> average(e) <= FATIGUE_THRESHOLD)) {
            alerts.add(new PlayerAlert(
                    "FATIGUE",
                    "Bien-être bas sur les " + WINDOW + " derniers jours renseignés",
                    "HIGH"
            ));
        }

        long painCount = lastN.stream().filter(e -> e.getSoreness() != null && e.getSoreness() <= 2).count();
        if (lastN.size() >= 2 && painCount >= 2) {
            String locations = lastN.stream()
                    .filter(e -> e.getSoreness() != null && e.getSoreness() <= 2 && e.getPainLocation() != null && !e.getPainLocation().isBlank())
                    .map(WellnessEntry::getPainLocation)
                    .distinct()
                    .collect(Collectors.joining(", "));
            String message = "Douleur signalée à plusieurs reprises" + (locations.isEmpty() ? "" : " (" + locations + ")");
            alerts.add(new PlayerAlert("DOULEUR", message, "HIGH"));
        }

        return alerts;
    }

    public boolean hasActiveAlerts(Long playerId) {
        return !computeAlerts(playerId).isEmpty();
    }

    private double average(WellnessEntry e) {
        return (e.getMood() + e.getSleep() + e.getFatigue() + e.getSoreness() + e.getStress()) / 5.0;
    }
}
