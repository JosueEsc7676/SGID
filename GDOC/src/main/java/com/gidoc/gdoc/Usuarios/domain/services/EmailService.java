package com.gidoc.gdoc.Usuarios.domain.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.name:Sistema GDOC}")
    private String appName;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean enviarEmailRecuperacion(String toEmail, String nombreUsuario, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Recuperación de Contraseña - " + appName);

            String contenido = String.format(
                    "Hola %s,\n\n" +
                            "Has solicitado recuperar tu contraseña en el %s.\n\n" +
                            "Tu código de recuperación es:\n\n" +
                            "🔑 **%s**\n\n" +
                            "Ingresa este código en la aplicación para establecer una nueva contraseña.\n\n" +
                            "Este código expirará en 24 horas.\n\n" +
                            "Si no solicitaste este cambio, ignora este mensaje.\n\n" +
                            "Saludos,\nEquipo %s",
                    nombreUsuario, appName, token, appName
            );

            message.setText(contenido);

            mailSender.send(message);
            log.info("✅ Email de recuperación enviado a: {}", toEmail);
            log.info("📧 Token generado: {}", token);
            return true;

        } catch (Exception e) {
            log.error("❌ Error al enviar email de recuperación", e);
            return false;
        }
    }
}