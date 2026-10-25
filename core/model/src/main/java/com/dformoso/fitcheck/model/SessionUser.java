package com.dformoso.fitcheck.model;

/**
 * El usuario con la sesion abierta.
 *
 * <p>{@code email} solo aparece aqui, en el propio perfil. Nunca se expone el correo de
 * otros usuarios: la busqueda es abierta y eso convertiria la app en un directorio de
 * direcciones. Para los demas existe {@link PublicUser}.
 *
 * <p>{@code avatarUrl} es nulo mientras no haya subido foto. Este modulo es Java puro y
 * no tiene anotaciones de nulabilidad, asi que queda dicho aqui.
 */
public record SessionUser(
        String id,
        String username,
        String email,
        String displayName,
        boolean emailVerified,
        String avatarUrl,
        boolean isPrivate,
        int followers,
        int following) {

    /** Lo que se ensena en pantalla: el nombre visible si lo hay, si no la arroba. */
    public String visibleName() {
        return displayName == null || displayName.isBlank() ? username : displayName;
    }

    /** Inicial para el avatar de respaldo mientras no haya foto. */
    public String initial() {
        String name = visibleName();
        return name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
    }
}
