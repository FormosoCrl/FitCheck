package com.dformoso.fitcheck.model;

import java.time.LocalDate;

/**
 * Un dia de la cuadricula de constancia.
 *
 * <p>La intensidad es el <b>numero de series</b>, no la asistencia binaria ni el volumen
 * en kilos: la asistencia solo daria dos tonos, y el volumen castigaria a quien entrena
 * tirones frente a quien hace sentadilla.
 */
public record HeatmapDay(LocalDate date, int sets, int workouts) {

    /** Niveles de intensidad, como en GitHub: 0 (sin entrenar) a 4. */
    public static final int MAX_LEVEL = 4;

    /**
     * Nivel relativo al mejor dia del periodo. Relativo y no absoluto porque un umbral
     * fijo pintaria de verde oscuro a cualquiera que haga mucho volumen y dejaria gris
     * al resto, cuando lo que interesa es la constancia de cada uno.
     */
    public int level(int maxSets) {
        if (sets <= 0 || maxSets <= 0) {
            return 0;
        }
        int level = (int) Math.ceil((double) sets / maxSets * MAX_LEVEL);
        return Math.min(MAX_LEVEL, Math.max(1, level));
    }
}
