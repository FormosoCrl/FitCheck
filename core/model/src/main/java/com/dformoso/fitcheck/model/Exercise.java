package com.dformoso.fitcheck.model;

import java.util.UUID;

/**
 * Un ejercicio con nombre libre.
 *
 * <p>El {@code id} lo genera el <b>cliente</b>, no el servidor. Es lo que permite crear un
 * ejercicio sin cobertura y que esa fila ya tenga identidad definitiva: al sincronizar no
 * hay que reasignar ids ni arreglar las series que lo referencian.
 *
 * @param global true si viene del catalogo comun del servidor (no editable)
 */
public record Exercise(String id, String name, boolean global) {

    public static Exercise create(String name) {
        return new Exercise(UUID.randomUUID().toString(), name.trim(), false);
    }
}
