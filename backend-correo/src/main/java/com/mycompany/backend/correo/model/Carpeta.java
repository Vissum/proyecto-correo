package com.mycompany.backend.correo.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Carpeta {

    private UUID id;
    private UUID usuarioId;
    private String nombre;
    private Boolean esSistema;
    private OffsetDateTime fechaCreacion;

    public Carpeta() {
    }

    public Carpeta(UUID id, UUID usuarioId, String nombre, Boolean esSistema, OffsetDateTime fechaCreacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.esSistema = esSistema;
        this.fechaCreacion = fechaCreacion;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getEsSistema() {
        return esSistema;
    }

    public void setEsSistema(Boolean esSistema) {
        this.esSistema = esSistema;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}