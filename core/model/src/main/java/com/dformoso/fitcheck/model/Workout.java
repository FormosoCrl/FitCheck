package com.dformoso.fitcheck.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Una sesion de gimnasio. Es la unidad de asistencia del heatmap.
 *
 * <p>Se permiten varias por dia a proposito: los dobles entrenos existen, y con ids
 * generados por el cliente una restriccion de unicidad por (usuario, fecha) haria fallar
 * la sincronizacion en lugar de resolverla.
 */
public record Workout(String id, LocalDate date, String note, List<WorkoutSet> sets) {

    public Workout {
        sets = sets == null ? List.of() : List.copyOf(sets);
    }

    public static Workout createFor(LocalDate date) {
        return new Workout(UUID.randomUUID().toString(), date, "", List.of());
    }

    public int setCount() {
        return sets.size();
    }

    public int totalReps() {
        return sets.stream().mapToInt(WorkoutSet::reps).sum();
    }
}
