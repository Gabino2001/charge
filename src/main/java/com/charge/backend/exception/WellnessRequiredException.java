package com.charge.backend.exception;

/**
 * Levée quand un joueur tente d'accéder à ses exercices ou d'en valider un
 * sans avoir rempli son questionnaire de bien-être du jour.
 * Mappée sur le statut HTTP 428 (Precondition Required).
 */
public class WellnessRequiredException extends RuntimeException {
    public WellnessRequiredException(String message) {
        super(message);
    }
}
