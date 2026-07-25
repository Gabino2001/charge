package com.charge.backend.service;

import com.charge.backend.dto.FicheDtos.RMEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Calcule la charge maximale théorique (1RM) à partir d'un test (poids x répétitions),
 * puis dérive le poids correspondant à chaque palier de pourcentage du 1RM.
 * Formule de Brzycki : 1RM = poids / (1.0278 - 0.0278 x répétitions)
 *
 * Le tableau de charges n'est plus indexé par un nombre de répétitions estimé :
 * il est exprimé en pourcentage du 1RM (100% à 0%, par palier de 5%). C'est ensuite
 * au préparateur de définir lui-même, pour chaque exercice, le nombre de répétitions
 * à réaliser sur le pourcentage choisi.
 */
public final class RMCalculator {

    private static final double STEP_KG = 2.5;
    private static final int MIN_PERCENTAGE = 0;
    private static final int MAX_PERCENTAGE = 100;
    private static final int PERCENTAGE_STEP = 5;

    private RMCalculator() {}

    public static double oneRepMax(double weight, int reps) {
        if (weight <= 0 || reps <= 0) return 0;
        if (reps == 1) return roundToStep(weight);
        double raw = weight / (1.0278 - 0.0278 * reps);
        return raw > 0 ? roundToStep(raw) : 0;
    }

    /** Tableau des charges par palier de %RM, du plus lourd (100%) au plus léger (50%). */
    public static List<RMEntry> rmTable(double oneRepMax) {
        List<RMEntry> table = new ArrayList<>();
        if (oneRepMax <= 0) return table;
        for (int percentage = MAX_PERCENTAGE; percentage >= MIN_PERCENTAGE; percentage -= PERCENTAGE_STEP) {
            double raw = oneRepMax * (percentage / 100.0);
            double weight = raw > 0 ? roundToStep(raw) : 0;
            table.add(new RMEntry(percentage, weight));
        }
        return table;
    }

    private static double roundToStep(double value) {
        return Math.round(value / STEP_KG) * STEP_KG;
    }
}
