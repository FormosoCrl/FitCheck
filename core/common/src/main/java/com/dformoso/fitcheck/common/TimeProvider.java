package com.dformoso.fitcheck.common;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Fuente de "ahora".
 *
 * <p>Existe porque todo el motor de energía razona sobre ventanas de días y hay que poder
 * simular 90 días de historial en un test que dura milisegundos. Ningún código de dominio
 * debe llamar a {@code LocalDate.now()} directamente.
 */
public interface TimeProvider {

    Instant now();

    ZoneId zone();

    default LocalDate today() {
        return LocalDate.ofInstant(now(), zone());
    }

    /** Implementación real. En tests se sustituye por una fija o controlable. */
    static TimeProvider system() {
        return new TimeProvider() {
            @Override
            public Instant now() {
                return Instant.now();
            }

            @Override
            public ZoneId zone() {
                return ZoneId.systemDefault();
            }
        };
    }
}
