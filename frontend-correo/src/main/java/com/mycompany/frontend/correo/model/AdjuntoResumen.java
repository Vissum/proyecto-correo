package com.mycompany.frontend.correo.model;

public record AdjuntoResumen(String id, String nombre, long tamanoBytes) {

    public String tamanoLegible() {
        if (tamanoBytes < 1024) {
            return tamanoBytes + " B";
        }
        if (tamanoBytes < 1024 * 1024) {
            return String.format("%.1f KB", tamanoBytes / 1024.0);
        }
        return String.format("%.1f MB", tamanoBytes / 1048576.0);
    }
}