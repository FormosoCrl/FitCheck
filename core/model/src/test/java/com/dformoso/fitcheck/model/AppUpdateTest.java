package com.dformoso.fitcheck.model;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class AppUpdateTest {

    private static AppUpdate version(int code, String name) {
        return new AppUpdate(code, name, "notas", "https://ejemplo/app.apk", 0);
    }

    @Test
    public void aHigherVersionCodeIsNewer() {
        assertThat(version(3, "1.2").isNewerThan(2)).isTrue();
    }

    @Test
    public void theSameVersionIsNotNewer() {
        assertThat(version(2, "1.1").isNewerThan(2)).isFalse();
    }

    @Test
    public void anOlderVersionIsNotNewer() {
        // Pasa al instalar a mano un APK mas nuevo que el publicado: no debe ofrecer
        // "actualizar" a una version anterior.
        assertThat(version(1, "1.0").isNewerThan(5)).isFalse();
    }

    /**
     * El motivo de comparar por numero y no por nombre.
     *
     * <p>Como texto, "1.10" es MENOR que "1.9" porque compara caracter a caracter. Si el
     * actualizador mirara el nombre, la version 1.10 nunca se ofreceria a quien tenga la
     * 1.9.
     */
    @Test
    public void comparingByNameWouldBeWrong() {
        assertThat("1.10".compareTo("1.9")).isLessThan(0);
        assertThat(version(10, "1.10").isNewerThan(9)).isTrue();
    }

    @Test
    public void sizeIsReportedInMegabytes() {
        AppUpdate update = new AppUpdate(3, "1.2", "", "https://ejemplo/app.apk", 10_485_760L);
        assertThat(update.sizeMb()).isWithin(0.01).of(10.0);
    }
}
