package com.dformoso.fitcheck.model;

/**
 * Como termino un registro.
 *
 * <p>El servidor puede responder de dos formas segun tenga activada o no la verificacion
 * por correo, y la app tiene que reaccionar distinto a cada una. Esto lo hace explicito
 * en vez de dejarlo a que el cliente adivine mirando si vino un token.
 *
 * @param sessionStarted        el registro dejo la sesion abierta: no hay que verificar nada
 * @param verificationEmailSent solo tiene sentido si {@code sessionStarted} es false
 */
public record RegisterOutcome(boolean sessionStarted, boolean verificationEmailSent) {

    public static RegisterOutcome loggedIn() {
        return new RegisterOutcome(true, false);
    }

    public static RegisterOutcome needsVerification(boolean emailSent) {
        return new RegisterOutcome(false, emailSent);
    }
}
