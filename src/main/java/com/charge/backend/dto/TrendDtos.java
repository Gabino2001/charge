package com.charge.backend.dto;

import java.time.LocalDate;
import java.util.List;

public class TrendDtos {

    /** Un point de courbe générique : une date et une valeur. */
    public record TrendPoint(LocalDate date, double value) {}

    public record TrendSeries(String label, List<TrendPoint> points) {}
}
