package com.charge.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Envoi d'emails transactionnels via l'API HTTP de Brevo (ex-Sendinblue).
 *
 * On utilise une API HTTP plutôt que du SMTP classique car de nombreux hébergeurs
 * (dont Render sur son plan gratuit) bloquent les ports SMTP sortants (25, 465, 587)
 * pour prévenir les abus. L'API HTTP passe par le port 443 (HTTPS), jamais bloqué.
 */
@Service
@Slf4j
public class EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.mail.brevo-api-key}")
    private String brevoApiKey;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.from-name:CHARGE.}")
    private String fromName;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        Map<String, Object> sender = new HashMap<>();
        sender.put("name", fromName);
        sender.put("email", fromAddress);

        Map<String, Object> recipient = new HashMap<>();
        recipient.put("email", toEmail);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", sender);
        body.put("to", List.of(recipient));
        body.put("subject", "CHARGE. — Réinitialisation de ton mot de passe");
        body.put("htmlContent", buildHtmlBody(resetLink));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        headers.set("accept", "application/json");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(BREVO_API_URL, HttpMethod.POST, request, String.class);
            log.info("[EMAIL] Email de réinitialisation envoyé à {}", toEmail);
        } catch (RestClientException ex) {
            log.error("[EMAIL] Échec de l'envoi à {} via Brevo : {}", toEmail, ex.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email de réinitialisation.", ex);
        }
    }

    private String buildHtmlBody(String resetLink) {
        return "<div style=\"font-family:Arial,sans-serif;max-width:480px;margin:0 auto;\">"
                + "<h2 style=\"color:#111;\">Réinitialisation de mot de passe</h2>"
                + "<p>Bonjour,</p>"
                + "<p>Tu as demandé à réinitialiser ton mot de passe sur <strong>CHARGE.</strong></p>"
                + "<p>Clique sur le bouton ci-dessous pour choisir un nouveau mot de passe (valable 1 heure) :</p>"
                + "<p style=\"text-align:center;margin:24px 0;\">"
                + "<a href=\"" + resetLink + "\" style=\"background:#e11d2e;color:#fff;padding:12px 24px;border-radius:24px;text-decoration:none;font-weight:bold;\">RÉINITIALISER MON MOT DE PASSE</a>"
                + "</p>"
                + "<p style=\"color:#888;font-size:12px;\">Si tu n'es pas à l'origine de cette demande, ignore simplement cet email.</p>"
                + "<p>— L'équipe CHARGE.</p>"
                + "</div>";
    }
}
