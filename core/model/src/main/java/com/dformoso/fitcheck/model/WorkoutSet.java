package com.dformoso.fitcheck.model;

import java.util.OptionalDouble;
import java.util.UUID;

/**
 * Una serie: un ejercicio, unas repeticiones y opcionalmente un peso.
 *
 * <p>El peso es opcional de verdad: dominadas, fondos y abdominales no lo llevan, y
 * forzar un cero falsearia cualquier calculo de volumen.
 */
public record WorkoutSet(
        String id,
        String workoutId,
        String exerciseId,
        String exerciseName,
        int position,
        int reps,
        OptionalDouble weightKg,
        boolean failed,
        boolean personalRecord) {

    public static final int MAX_REPS = 1000;

    public WorkoutSet {
        if (reps < 1 || reps > MAX_REPS) {
            throw new IllegalArgumentException("Repeticiones fuera de rango: " + reps);
        }
        if (weightKg == null) {
            throw new IllegalArgumentException("usa OptionalDouble.empty(), nunca null");
        }
        // Un intento que no se saco no puede ser un record. Permitir las dos marcas a la
        // vez dejaria series que dicen "no la saque" y "mi mejor marca" al mismo tiempo.
        if (failed && personalRecord) {
            throw new IllegalArgumentException(
                    "una serie no puede ser fallada y record a la vez");
        }
    }

    public static WorkoutSet create(String workoutId, Exercise exercise, int position,
                                    int reps, OptionalDouble weightKg,
                                    boolean failed, boolean personalRecord) {
        return new WorkoutSet(
                UUID.randomUUID().toString(),
                workoutId,
                exercise.id(),
                exercise.name(),
                position,
                reps,
                weightKg,
                failed,
                personalRecord);
    }

    /**
     * Repeticiones por peso. Vacio si la serie no lleva peso o si fue un intento fallado.
     *
     * <p>Un fallo no suma volumen: contarlo inflaria el total con trabajo que no se
     * llego a hacer. Pero la serie se guarda igual, porque haberlo intentado es
     * informacion.
     */
    public OptionalDouble volumeKg() {
        if (failed || weightKg.isEmpty()) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(reps * weightKg.getAsDouble());
    }
}
