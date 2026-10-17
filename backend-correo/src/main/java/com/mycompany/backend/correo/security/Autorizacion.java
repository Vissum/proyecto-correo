package com.mycompany.backend.correo.security;

import java.util.UUID;

public final class Autorizacion {

    private Autorizacion() {
    }

    // Convierte el encabezado X-Usuario-Id en UUID. Devuelve null si no es válido.
    public static UUID leer(String encabezado) {
        if (encabezado == null || encabezado.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(encabezado.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // True solo si el encabezado corresponde al usuario que se quiere usar.
    public static boolean esMismoUsuario(String encabezado, UUID usuarioId) {
        UUID solicitante = leer(encabezado);
        return solicitante != null && solicitante.equals(usuarioId);
    }
}