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
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject("🔐 Código de verificación - AddVenture");

            Context context = new Context();
            context.setVariable("codigo", codigo);

            String htmlContent = templateEngine.process("email-verificacion", context);
            helper.setText(htmlContent, true);

            ClassPathResource recursoImagen = new ClassPathResource("static/img/AddVenture_white.png");
            
            helper.addInline("logoImagen", recursoImagen);

            mailSender.send(mensaje);

        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo HTML", e);
        }

    }

}
