package com.dformoso.fitcheck.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Las series de un mismo ejercicio dentro de un dia.
 *
 * <p>Un entreno se lee por ejercicio, no por serie suelta: quien mira "press banca" quiere
 * ver sus cuatro series juntas para comparar el peso entre ellas, no intercaladas con las
 * dominadas.
 *
 * <p>Vive en {@code :core:model} porque agrupar es dominio, no pintura, y lo necesitan dos
 * pantallas que no pueden depender la una de la otra.
 */
public record ExerciseGroup(String exerciseName, List<WorkoutSet> sets) {

    public ExerciseGroup {
        sets = sets == null ? List.of() : List.copyOf(sets);
    }

    public int setCount() {
        return sets.size();
    }

    /**
     * Agrupa por ejercicio conservando el orden de aparicion.
     *
     * <p>Se agrupa por identificador y no por nombre: el nombre es lo que se pinta, pero dos
     * ejercicios distintos podrian mostrarse igual y acabarian mezclados.
     *
     * <p>Las series que no son consecutivas tambien caen en el mismo grupo. Es deliberado:
     * en una superserie se alternan dos ejercicios, y verlos repartidos en seis cabeceras no
     * ayuda a nadie.
     */
    public static List<ExerciseGroup> groupByExercise(List<WorkoutSet> sets) {
        if (sets == null || sets.isEmpty()) {
            return List.of();
        }

        Map<String, List<WorkoutSet>> byExercise = new LinkedHashMap<>();
        for (WorkoutSet set : sets) {
            // Si el id viniera vacio, el nombre es el mejor criterio que queda: peor seria
            // juntar en un grupo todos los ejercicios sin id.
            String key = set.exerciseId() == null || set.exerciseId().isBlank()
                    ? "name:" + set.exerciseName()
                    : set.exerciseId();
            byExercise.computeIfAbsent(key, ignored -> new ArrayList<>()).add(set);
        }

        List<ExerciseGroup> groups = new ArrayList<>(byExercise.size());
        for (List<WorkoutSet> grouped : byExercise.values()) {
            groups.add(new ExerciseGroup(grouped.get(0).exerciseName(), grouped));
        }
        return groups;
    }
}
