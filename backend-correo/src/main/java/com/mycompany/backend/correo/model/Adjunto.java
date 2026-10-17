package com.mycompany.backend.correo.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Adjunto {

    private UUID id;
    private UUID mensajeId;
    private String nombreArchivo;
    private String tipoMime;
    private Long tamanoBytes;
    private String rutaServidor;
    private OffsetDateTime fechaSubida;

    public Adjunto() {
    }

    public Adjunto(UUID id, UUID mensajeId, String nombreArchivo, String tipoMime, Long tamanoBytes, String rutaServidor, OffsetDateTime fechaSubida) {
        this.id = id;
        this.mensajeId = mensajeId;
        this.nombreArchivo = nombreArchivo;
        this.tipoMime = tipoMime;
        this.tamanoBytes = tamanoBytes;
        this.rutaServidor = rutaServidor;
        this.fechaSubida = fechaSubida;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMensajeId() {
        return mensajeId;
    }

    public void setMensajeId(UUID mensajeId) {
        this.mensajeId = mensajeId;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getTipoMime() {
        return tipoMime;
    }

    public void setTipoMime(String tipoMime) {
        this.tipoMime = tipoMime;
    }

    public Long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(Long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public String getRutaServidor() {
        return rutaServidor;
    }

    public void setRutaServidor(String rutaServidor) {
        this.rutaServidor = rutaServidor;
    }

    public OffsetDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(OffsetDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }
}