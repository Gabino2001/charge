package com.charge.backend.domain;

/**
 * Type de séance de travail du jour, choisi par le préparateur :
 * - ATELIER : les exercices s'enchaînent avec un temps de récupération entre chaque.
 * - SUPERSET : les exercices s'enchaînent sans repos, un seul temps de récupération
 *   est pris à la fin du bloc (avant de recommencer un tour).
 */
public enum SessionType {
    ATELIER,
    SUPERSET
}
