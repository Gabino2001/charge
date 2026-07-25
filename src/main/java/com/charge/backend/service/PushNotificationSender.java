package com.charge.backend.service;

import com.charge.backend.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Point d'intégration pour l'envoi de notifications push via Firebase Cloud Messaging.
 *
 * Pour activer l'envoi réel :
 *  1. Ajouter la dépendance "com.google.firebase:firebase-admin" au pom.xml
 *  2. Initialiser FirebaseApp avec le fichier de clé de service (variable d'environnement,
 *     ex. GOOGLE_APPLICATION_CREDENTIALS)
 *  3. Remplacer le corps de send() par un appel à FirebaseMessaging.getInstance().send(message),
 *     en utilisant le token FCM stocké pour l'utilisateur (à ajouter sur l'entité User).
 *
 * Le stub actuel se contente de journaliser l'envoi, pour ne pas bloquer le développement
 * du reste de l'application tant que Firebase n'est pas configuré.
 */
@Component
@Slf4j
public class PushNotificationSender {

    public void send(User recipient, String title, String body) {
        log.info("[PUSH] -> {} ({}) : {} — {}", recipient.getFullName(), recipient.getEmail(), title, body);
        // TODO : intégration Firebase Cloud Messaging (voir javadoc ci-dessus)
    }
}
