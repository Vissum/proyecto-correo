package com.mycompany.backend.correo.service;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@ApplicationScoped
public class MailService {

    @Resource(lookup = "mail/CorreoMail")
    private Session sesion;

    // Base de la aplicación web, configurable con: -Dcorreo.frontend.url=http://IP:8080/CONTEXTO
    public static String urlFrontend() {
        return System.getProperty("correo.frontend.url", "http://localhost:8080/frontend-correo-1.0-SNAPSHOT");
    }

    public void enviarRecuperacion(String destino, String nombre, String enlace) {
        String saludo = nombre == null || nombre.isBlank() ? "" : " " + nombre;
        String html = "<p>Hola" + escaparHtml(saludo) + ":</p>"
                + "<p>Recibimos una solicitud para restablecer tu contraseña.</p>"
                + "<p><a href=\"" + escaparHtml(enlace) + "\">Restablecer contraseña</a></p>"
                + "<p>El enlace vence en 30 minutos y solo puede usarse una vez. "
                + "Si no fuiste tú, ignora este mensaje.</p>";
        try {
            MimeMessage mensaje = new MimeMessage(sesion);
            mensaje.setFrom();
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destino));
            mensaje.setSubject("Recuperación de contraseña", "UTF-8");
            mensaje.setContent(html, "text/html; charset=UTF-8");
            Transport.send(mensaje);
        } catch (MessagingException e) {
            throw new IllegalStateException("No se pudo enviar el correo de recuperación.", e);
        }
    }

    private static String escaparHtml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;").replace("\"", "&quot;");
    }
}