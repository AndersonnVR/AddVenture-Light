package com.addventure.AddVenture.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para toda la capa REST.
 *
 * @RestControllerAdvice hace que esta clase intercepte automáticamente
 *                       cualquier excepción lanzada desde los controladores
 *                       REST.
 *
 *                       Propósito:
 *                       - Unificar el formato de respuestas de error.
 *                       - Evitar escribir try/catch repetitivos en cada
 *                       endpoint.
 *                       - Devolver JSON consistente al frontend en caso de
 *                       errores.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación generados cuando un DTO anotado con @Valid
     * no cumple las restricciones (ej: @NotBlank, @Email, @Size, etc).
     *
     * Captura MethodArgumentNotValidException, extrae los mensajes de error
     * por campo y devuelve un JSON con status 400.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> onValidationError(MethodArgumentNotValidException ex) {

        // Extraer mensajes de validación de cada campo inválido
        var messages = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()) // Ejemplo: "email: formato inválido"
                .collect(Collectors.toList());

        // Enviar JSON limpio con todos los errores
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                java.util.Map.of("errors", messages));
    }

    /**
     * Captura cualquier otra excepción NO manejada explícitamente.
     *
     * Evita que el backend devuelva HTML de error o trazas internas,
     * y en su lugar envía un JSON controlado al cliente.
     *
     * Útil para:
     * - NullPointerException
     * - IllegalStateException
     * - Errores inesperados de lógica
     * - Cualquier excepción runtime
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> onAny(Exception ex) {

        // Respuesta genérica para errores inesperados
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                java.util.Map.of(
                        "message", "Error interno",
                        "detail", ex.getMessage() // mensaje técnico, puede ocultarse en producción
                ));
    }

}