package com.charge.backend.domain;

public enum TrainingSessionStatus {
    /** La séance est en cours : au moins un exercice reste à faire, ou le RPE n'a pas encore été envoyé. */
    IN_PROGRESS,
    /** Le joueur a validé la fin de séance et envoyé son ressenti (RPE) global. */
    COMPLETED
}
