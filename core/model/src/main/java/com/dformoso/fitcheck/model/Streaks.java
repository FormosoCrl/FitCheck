package com.dformoso.fitcheck.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Rachas de dias entrenados consecutivos.
 *
 * <p>Java puro y sin estado: se puede probar sin emulador y sin base de datos.
 *
 * @param current racha en curso
 * @param longest la mas larga del periodo
 */
public record Streaks(int current, int longest) {

    public static final Streaks EMPTY = new Streaks(0, 0);

    /**
     * Calcula las rachas a partir de los dias con actividad.
     *
     * <p>La racha en curso admite que hoy todavia no se haya entrenado: si ayer si, sigue
     * viva. Cortarla a las 00:00 seria absurdo, porque al usuario aun le queda el dia por
     * delante para ir al gimnasio.
     *
     * @param today referencia de "hoy", inyectada para poder fijarla en los tests
     */
    public static Streaks from(List<HeatmapDay> days, LocalDate today) {
        if (days == null || days.isEmpty()) {
            return EMPTY;
        }

        Set<LocalDate> trained = new TreeSet<>();
        for (HeatmapDay day : days) {
            if (day.sets() > 0) {
                trained.add(day.date());
            }
        }
        if (trained.isEmpty()) {
            return EMPTY;
        }

        int longest = 1;
        int run = 1;
        LocalDate previous = null;
        for (LocalDate date : trained) {
            if (previous != null) {
                run = ChronoUnit.DAYS.between(previous, date) == 1 ? run + 1 : 1;
                longest = Math.max(longest, run);
            }
            previous = date;
        }

        return new Streaks(currentStreak(trained, today), longest);
    }

    private static int currentStreak(Set<LocalDate> trained, LocalDate today) {
        // Se empieza en hoy si hay entreno, y si no en ayer: el dia en curso todavia
        // no cuenta como fallado.
        LocalDate cursor = trained.contains(today) ? today : today.minusDays(1);
        int streak = 0;
        while (trained.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
