package com.dformoso.fitcheck.model;

/** Totales de un perfil: lo que se ensena en la cabecera de ajustes. */
public record ProfileStats(
        int daysTrained,
        int totalSets,
        int distinctExercises,
        int followers,
        int following) {

    public static final ProfileStats EMPTY = new ProfileStats(0, 0, 0, 0, 0);
}
