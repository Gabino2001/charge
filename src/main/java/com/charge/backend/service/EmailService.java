package com.charge.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("CHARGE. — Réinitialisation de ton mot de passe");
            message.setText(
                    "Bonjour,\n\n" +
                            "Tu as demandé à réinitialiser ton mot de passe sur CHARGE.\n\n" +
                            "Clique sur le lien ci-dessous pour choisir un nouveau mot de passe " +
                            "(valable 1 heure) :\n\n" +
                            resetLink + "\n\n" +
                            "Si tu n'es pas à l'origine de cette demande, ignore simplement cet email.\n\n" +
                            "— L'équipe CHARGE."
            );
            mailSender.send(message);
            log.info("[EMAIL] Email de réinitialisation envoyé à {}", toEmail);
        } catch (Exception ex) {
            log.error("[EMAIL] Échec de l'envoi à {} : {}", toEmail, ex.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email de réinitialisation.", ex);
        }
    }
}
