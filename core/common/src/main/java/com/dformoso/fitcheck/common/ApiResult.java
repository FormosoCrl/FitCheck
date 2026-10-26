package com.dformoso.fitcheck.common;

/**
 * Resultado de una operación que habla con el servidor.
 *
 * <p>Interfaz sellada: el compilador obliga a cubrir los dos casos en un {@code switch},
 * así que no se puede olvidar el de error. Es la razón de que exista en vez de devolver
 * el valor y {@code null} cuando falla.
 *
 * <p>El {@link Failure#code()} es lo que permite reaccionar de forma distinta a cada
 * problema sin leer el texto del mensaje, que está pensado para el usuario y cambia.
 */
public sealed interface ApiResult<T> {

    /** Códigos de error que la interfaz trata de forma específica. */
    String CODE_NETWORK = "network";
    String CODE_INVALID_CREDENTIALS = "invalid_credentials";
    String CODE_EMAIL_NOT_VERIFIED = "email_not_verified";
    String CODE_INVALID_CODE = "invalid_code";
    String CODE_COOLDOWN = "cooldown";
    String CODE_VALIDATION = "validation";
    String CODE_UNKNOWN = "unknown";

    record Success<T>(T value) implements ApiResult<T> {
    }

    /**
     * @param code    identificador estable, para decidir qué hacer
     * @param message texto para enseñar al usuario
     * @param field   campo culpable si el error es de validación; puede ser null
     */
    record Failure<T>(String code, String message, String field) implements ApiResult<T> {

        public Failure(String code, String message) {
            this(code, message, null);
        }
    }

    static <T> ApiResult<T> success(T value) {
        return new Success<>(value);
    }

    static <T> ApiResult<T> failure(String code, String message) {
        return new Failure<>(code, message);
    }

    default boolean isSuccess() {
        return this instanceof Success<T>;
    }

    /** Callback de una operación asíncrona. Se invoca en un hilo de fondo. */
    interface Callback<T> {
        void onResult(ApiResult<T> result);
    }
}
