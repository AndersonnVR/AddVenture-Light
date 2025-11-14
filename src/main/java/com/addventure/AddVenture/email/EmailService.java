package com.addventure.AddVenture.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final String from;

    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine,
            @Value("${app.verification.from-address:no-reply@example.com}") String from) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.from = from;
    }

    /*
     * Envia email HTML con el codigo. Si falla el envio HTML, intenta enviar texto
     * simple.
     */
    public void sendVerificationCode(String to, String code, int ttlMinutes) {
        try {
            sendHtmlEmail(to, code, ttlMinutes);
        } catch (Exception ex) {
            // Fallback: enviar texto simple
            sendTextEmail(to, code, ttlMinutes);
        }
    }

    private void sendHtmlEmail(String to, String code, int ttlMinutes) {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, "utf-8");
        Context ctx = new Context();
        ctx.setVariable("code", code);
        ctx.setVariable("ttlMinutes", ttlMinutes);
        String html = templateEngine.process("verification-email", ctx);
        try {
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Tu codigo de verificacion");
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (MessagingException e) {
            throw new RuntimeException("Error creando el email", e);
        }
    }

    private void sendTextEmail(String to, String code, int ttlMinutes) {
        MimeMessage msg = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");
            String text = String.format(
                    "Tu código de verificación es: %s\nExpira en %d minutos.\nSi no solicitaste esto, ignora este correo.",
                    code, ttlMinutes);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Tu código de verificación");
            helper.setText(text, false);
            mailSender.send(msg);
        }catch(MessagingException e){
            throw new RuntimeException("Error enviando email", e);
        }
    }

}
