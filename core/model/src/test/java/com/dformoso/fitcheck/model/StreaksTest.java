package com.dformoso.fitcheck.model;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StreaksTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 17);

    private static List<HeatmapDay> days(int... daysAgo) {
        List<HeatmapDay> result = new ArrayList<>();
        for (int offset : daysAgo) {
            result.add(new HeatmapDay(TODAY.minusDays(offset), 5, 1));
        }
        return result;
    }

    @Test
    public void sinDatos_devuelveCero() {
        assertThat(Streaks.from(List.of(), TODAY)).isEqualTo(Streaks.EMPTY);
        assertThat(Streaks.from(null, TODAY)).isEqualTo(Streaks.EMPTY);
    }

    @Test
    public void diasConsecutivosTerminandoHoy() {
        Streaks streaks = Streaks.from(days(0, 1, 2), TODAY);
        assertThat(streaks.current()).isEqualTo(3);
        assertThat(streaks.longest()).isEqualTo(3);
    }

    @Test
    public void laRachaSigueVivaSiHoyAunNoSeHaEntrenado() {
        // Regla deliberada: a las 00:00 no se corta la racha, todavia queda el dia
        // por delante para ir al gimnasio.
        Streaks streaks = Streaks.from(days(1, 2, 3), TODAY);
        assertThat(streaks.current()).isEqualTo(3);
    }

    @Test
    public void unDiaSaltadoCortaLaRachaActual() {
        Streaks streaks = Streaks.from(days(2, 3, 4), TODAY);
        assertThat(streaks.current()).isEqualTo(0);
        assertThat(streaks.longest()).isEqualTo(3);
    }

    @Test
    public void laRachaMasLargaNoTieneQueSerLaActual() {
        // 5 dias seguidos hace un mes, 2 ahora mismo.
        List<HeatmapDay> days = days(0, 1, 30, 31, 32, 33, 34);
        Streaks streaks = Streaks.from(days, TODAY);
        assertThat(streaks.current()).isEqualTo(2);
        assertThat(streaks.longest()).isEqualTo(5);
    }

    @Test
    public void losDiasSinSeriesNoCuentan() {
        List<HeatmapDay> days = List.of(
                new HeatmapDay(TODAY, 3, 1),
                new HeatmapDay(TODAY.minusDays(1), 0, 0),   // entreno vacio
                new HeatmapDay(TODAY.minusDays(2), 4, 1));
        Streaks streaks = Streaks.from(days, TODAY);
        assertThat(streaks.current()).isEqualTo(1);
    }

    @Test
    public void nivelDeIntensidadEsRelativoAlMejorDia() {
        HeatmapDay day = new HeatmapDay(TODAY, 5, 1);
        assertThat(day.level(20)).isEqualTo(1);
        assertThat(day.level(5)).isEqualTo(HeatmapDay.MAX_LEVEL);
        assertThat(new HeatmapDay(TODAY, 0, 0).level(20)).isEqualTo(0);
    }
}
