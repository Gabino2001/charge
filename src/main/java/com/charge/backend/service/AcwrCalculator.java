package com.charge.backend.service;

import com.charge.backend.domain.TrainingSession;
import com.charge.backend.dto.TrainingSessionDtos.AcwrResponse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcule le ratio de charge aiguë/chronique (ACWR), un indicateur classique en préparation physique
 * pour repérer un risque de blessure lié à une hausse trop rapide de la charge d'entraînement.
 * - Charge aiguë : moyenne journalière de charge sur les 7 derniers jours.
 * - Charge chronique : moyenne journalière de charge sur les 28 derniers jours.
 * - Ratio < 0.8 : charge en baisse (désentraînement). 0.8 à 1.3 : zone optimale.
 *   1.3 à 1.5 : à surveiller. > 1.5 : risque élevé de blessure.
 */
public final class AcwrCalculator {

    private AcwrCalculator() {}

    public static AcwrResponse compute(List<TrainingSession> recentSessions) {
        LocalDate today = LocalDate.now();

        Map<LocalDate, Integer> loadByDate = new HashMap<>();
        boolean hasAnyLoad = false;
        for (TrainingSession s : recentSessions) {
            Integer load = s.getTrainingLoad();
            if (load == null) continue;
            hasAnyLoad = true;
            loadByDate.merge(s.getSessionDate(), load, Integer::sum);
        }

        double acuteSum = 0;
        for (int i = 0; i < 7; i++) {
            acuteSum += loadByDate.getOrDefault(today.minusDays(i), 0);
        }
        double chronicSum = 0;
        for (int i = 0; i < 28; i++) {
            chronicSum += loadByDate.getOrDefault(today.minusDays(i), 0);
        }

        double acuteAvg = round1(acuteSum / 7.0);
        double chronicAvg = round1(chronicSum / 28.0);

        Double ratio = (hasAnyLoad && chronicAvg > 0) ? round1(acuteAvg / chronicAvg) : null;
        String zone = zoneFor(ratio);

        return new AcwrResponse(acuteAvg, chronicAvg, ratio, zone);
    }

    private static String zoneFor(Double ratio) {
        if (ratio == null) return "INSUFFICIENT_DATA";
        if (ratio < 0.8) return "LOW";
        if (ratio <= 1.3) return "OPTIMAL";
        if (ratio <= 1.5) return "ELEVATED";
        return "HIGH_RISK";
    }

    private static double round1(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
