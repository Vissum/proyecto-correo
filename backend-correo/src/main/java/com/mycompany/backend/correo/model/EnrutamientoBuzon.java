package com.mycompany.backend.correo.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class EnrutamientoBuzon {

    private UUID id;
    private UUID mensajeId;
    private UUID destinatarioId;
    private UUID carpetaId;
    private String tipoEnvio;
    private Boolean leido;
    private Boolean destacado;
    private OffsetDateTime fechaRecepcion;

    public EnrutamientoBuzon() {
    }

    public EnrutamientoBuzon(UUID id, UUID mensajeId, UUID destinatarioId, UUID carpetaId, String tipoEnvio, Boolean leido, Boolean destacado, OffsetDateTime fechaRecepcion) {
        this.id = id;
        this.mensajeId = mensajeId;
        this.destinatarioId = destinatarioId;
        this.carpetaId = carpetaId;
        this.tipoEnvio = tipoEnvio;
        this.leido = leido;
        this.destacado = destacado;
        this.fechaRecepcion = fechaRecepcion;
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

    public UUID getDestinatarioId() {
        return destinatarioId;
    }

    public void setDestinatarioId(UUID destinatarioId) {
        this.destinatarioId = destinatarioId;
    }

    public UUID getCarpetaId() {
        return carpetaId;
    }

    public void setCarpetaId(UUID carpetaId) {
        this.carpetaId = carpetaId;
    }

    public String getTipoEnvio() {
        return tipoEnvio;
    }

    public void setTipoEnvio(String tipoEnvio) {
        this.tipoEnvio = tipoEnvio;
    }

    public Boolean getLeido() {
        return leido;
    }

    public void setLeido(Boolean leido) {
        this.leido = leido;
    }

    public Boolean getDestacado() {
        return destacado;
    }

    public void setDestacado(Boolean destacado) {
        this.destacado = destacado;
    }

    public OffsetDateTime getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(OffsetDateTime fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }
}