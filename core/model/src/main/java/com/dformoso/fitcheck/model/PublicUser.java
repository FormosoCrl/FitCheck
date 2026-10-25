package com.dformoso.fitcheck.model;

/**
 * Lo que se ve de otro usuario. Sin correo, deliberadamente.
 *
 * <p>{@code avatarUrl} es nulo si esa persona no tiene foto. Este modulo es Java puro y
 * no tiene anotaciones de nulabilidad, asi que queda dicho aqui.
 */
public record PublicUser(
        String id,
        String username,
        String name,
        String avatarUrl,
        boolean isPrivate,
        FollowState followState) {

    public PublicUser {
        followState = followState == null ? FollowState.NONE : followState;
    }

    public String visibleName() {
        return name == null || name.isBlank() ? username : name;
    }

    /** Inicial para el avatar de respaldo mientras no haya foto. */
    public String initial() {
        String visible = visibleName();
        return visible.isEmpty() ? "?" : visible.substring(0, 1).toUpperCase();
    }
}
