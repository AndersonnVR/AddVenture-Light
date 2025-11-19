package com.addventure.AddVenture.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.addventure.AddVenture.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void enviarCorreoVerificacion(String destinatario, String codigo) {

        MimeMessage mensaje = mailSender.createMimeMessage();

        try {
            // El 'true' indica que es multipart (permite adjuntos/imágenes)
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject("🔐 Código de verificación - AddVenture");

            // 1. Preparamos el contexto (las variables para el HTML)
            Context context = new Context();
            context.setVariable("codigo", codigo);

            // 2. Procesamos la plantilla HTML con las variables
            String htmlContent = templateEngine.process("email-verificacion", context);
            helper.setText(htmlContent, true); // true indica que es HTML

            // 3. Incrustamos la imagen (CID)
            // Asegúrate que la ruta coincida con donde tienes tu imagen
            ClassPathResource recursoImagen = new ClassPathResource("static/img/AddVenture_white.png");

            // "logoImagen" debe coincidir con el cid:logoImagen del HTML
            helper.addInline("logoImagen", recursoImagen);

            mailSender.send(mensaje);

        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo HTML", e);
        }

    }

}
