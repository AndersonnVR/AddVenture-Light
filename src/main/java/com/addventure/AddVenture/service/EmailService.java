package com.addventure.AddVenture.service;

public interface EmailService {

    void enviarCorreoVerificacion(String destinatario, String codigo);
    
}
