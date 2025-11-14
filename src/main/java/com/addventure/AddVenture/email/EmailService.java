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

    /**
     * Componente de Spring (configurado con spring.mail.*) que crea y envía
     * mensajes MIME.
     */
    private final JavaMailSender mailSender;

    /**
     * motor Thymeleaf para procesar plantillas (busca los templates en
     * src/main/resources/templates).
     */
    private final SpringTemplateEngine templateEngine;

    private final String from;

    /**
     * @Value(...) String from: lee la propiedad app.verification.from-address de
     * application.yml/.properties. Si no existe, usa no-reply@example.com.
     */
    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine,
            @Value("${app.verification.from-address:no-reply@example.com}") String from) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.from = from;
    }

    /**
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

        /**
         * Crea un objeto de mensaje MIME vacío (cabeceras + cuerpo multipart posible).
         * No envía aún.
         */
        MimeMessage msg = mailSender.createMimeMessage();

        /**
         * MimeMessageHelper es una utilidad que facilita construir el MIME
         * (encabezados, texto/HTML, archivos adjuntos).
         * 
         * El "utf-8" indica la codificación a usar en el cuerpo. (Existe otro
         * constructor MimeMessageHelper(msg, true, "utf-8") si quieres
         * multipart/adjuntos.)
         */
        MimeMessageHelper helper = new MimeMessageHelper(msg, "utf-8");

        /**
         * Crea el contexto de Thymeleaf y define variables que serán usadas en la
         * plantilla verification-email.html como ${code} y ${ttlMinutes}.
         */
        Context ctx = new Context();
        ctx.setVariable("code", code);
        ctx.setVariable("ttlMinutes", ttlMinutes);

        /**
         * Thymeleaf procesa src/main/resources/templates/verification-email.html y
         * devuelve el HTML final con las variables reemplazadas.
         */
        String html = templateEngine.process("verification-email", ctx);
        try {
            helper.setFrom(from); // Coloca el remitente
            helper.setTo(to); // Destinatario
            helper.setSubject("Tu codigo de verificacion"); // Asunto del mail
            helper.setText(html, true); // Establece el cuerpo: el segundo parametro true indica que es HTML.
            mailSender.send(msg); // Envia realmente el mensaje mediante el JavaMailSender configurado

            /**
             * Si hay un problema creando el mensaje (dirección inválida, error en helper,
             * etc.), se envuelve en RuntimeException y sale al sendVerificationCode, que a
             * su vez atrapará y ejecutará el fallback.
             */
        } catch (MessagingException e) {
            throw new RuntimeException("Error creando el email", e);
        }
    }

    private void sendTextEmail(String to, String code, int ttlMinutes) {

        // Crea otro MimeMessage.
        MimeMessage msg = mailSender.createMimeMessage();
        try {
            // false indica que NO es multipart (sin adjuntos).
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");

            // Construye un String simple con el código y TTL.
            String text = String.format(
                    "Tu código de verificación es: %s\nExpira en %d minutos.\nSi no solicitaste esto, ignora este correo.",
                    code, ttlMinutes);

            // Setea from, to, subject.
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Tu código de verificación");
            helper.setText(text, false); // segundo parámetro false indica texto plano (no HTML).
            mailSender.send(msg); // Envía con mailSender.send(msg).

            /*
             * Si falla, lanza RuntimeException — en este punto el método público ya terminó
             * y la excepción subirá al llamador (posible manejo en controlador).
             */
        } catch (MessagingException e) {
            throw new RuntimeException("Error enviando email", e);
        }
    }
}
