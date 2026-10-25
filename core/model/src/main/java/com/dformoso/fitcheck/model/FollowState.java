package com.dformoso.fitcheck.model;

/**
 * Relacion con otra persona. Es lo que decide que pone el boton.
 *
 * <p>{@link #REQUESTED} existe porque las cuentas privadas no se siguen al momento: hay
 * un estado intermedio en el que ya has pedido pero todavia no te ven.
 */
public enum FollowState {
    NONE,
    REQUESTED,
    FOLLOWING,
    SELF;

    /** Traduce lo que manda el servidor. Un valor desconocido se trata como NONE. */
    public static FollowState from(String raw) {
        if (raw == null) {
            return NONE;
        }
        return switch (raw) {
            case "following" -> FOLLOWING;
            case "requested" -> REQUESTED;
            case "self" -> SELF;
            default -> NONE;
        };
    }
}
