package com.mycompany.frontend.correo.model;

public record CorreoResumen(String enrutamientoId, String mensajeId, String asunto,
                            String cuerpo, String fecha, boolean leido, boolean destacado) {

    public CorreoResumen(String enrutamientoId, String mensajeId, String asunto,
                         String cuerpo, String fecha, boolean leido) {
        this(enrutamientoId, mensajeId, asunto, cuerpo, fecha, leido, false);
    }

    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}