package com.charge.backend.service;

import com.charge.backend.dto.FicheDtos.RMEntry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RMCalculatorTest {

    @Test
    void oneRepMax_returnsWeightDirectly_whenRepsIsOne() {
        assertEquals(100.0, RMCalculator.oneRepMax(100, 1));
    }

    @Test
    void oneRepMax_appliesBrzyckiFormula_forMultipleReps() {
        // 1RM = 100 / (1.0278 - 0.0278 * 5) ≈ 112.5 kg (arrondi au 2.5 kg le plus proche)
        double result = RMCalculator.oneRepMax(100, 5);
        assertEquals(112.5, result, 0.01);
    }

    @Test
    void oneRepMax_returnsZero_whenWeightOrRepsInvalid() {
        assertEquals(0, RMCalculator.oneRepMax(0, 5));
        assertEquals(0, RMCalculator.oneRepMax(100, 0));
        assertEquals(0, RMCalculator.oneRepMax(-10, 5));
    }

    @Test
    void rmTable_hasElevenEntries_from100To50Percent() {
        List<RMEntry> table = RMCalculator.rmTable(112.5);
        assertEquals(11, table.size());
        assertEquals(100, table.get(0).percentage());
        assertEquals(50, table.get(10).percentage());
    }

    @Test
    void rmTable_weightDecreasesAsPercentageDecreases() {
        List<RMEntry> table = RMCalculator.rmTable(150);
        for (int i = 1; i < table.size(); i++) {
            assertTrue(table.get(i).weight() <= table.get(i - 1).weight(),
                    "Le poids doit diminuer (ou rester égal) quand le pourcentage de RM diminue");
        }
    }

    @Test
    void rmTable_isEmpty_whenOneRepMaxIsZero() {
        assertTrue(RMCalculator.rmTable(0).isEmpty());
    }
}
