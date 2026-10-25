package com.dformoso.fitcheck.model;

/**
 * Una solicitud de seguimiento pendiente.
 *
 * <p>Solo existen hacia cuentas privadas: seguir a una publica es inmediato y no genera
 * nada que aprobar.
 *
 * @param incoming true si te la han enviado a ti (puedes aceptar o rechazar); false si la
 *                 enviaste tu y sigue esperando respuesta (solo puedes retirarla)
 */
public record FriendRequest(String id, PublicUser other, boolean incoming) {
}
