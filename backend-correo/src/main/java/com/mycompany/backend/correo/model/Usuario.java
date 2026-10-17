package com.mycompany.backend.correo.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Usuario {

    private UUID id;
    private String correo;
    private String contrasenaHash;
    private String nombreCompleto;
    private String estado;
    private Integer cuotaAlmacenamientoMb;
    private Boolean esAdministrador;
    private OffsetDateTime fechaCreacion;

    public Usuario() {
    }

    public Usuario(UUID id, String correo, String contrasenaHash, String nombreCompleto, String estado,
                   Integer cuotaAlmacenamientoMb, OffsetDateTime fechaCreacion) {
        this.id = id;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.nombreCompleto = nombreCompleto;
        this.estado = estado;
        this.cuotaAlmacenamientoMb = cuotaAlmacenamientoMb;
        this.fechaCreacion = fechaCreacion;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public void setContrasenaHash(String contrasenaHash) {
        this.contrasenaHash = contrasenaHash;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getCuotaAlmacenamientoMb() {
        return cuotaAlmacenamientoMb;
    }

    public void setCuotaAlmacenamientoMb(Integer cuotaAlmacenamientoMb) {
        this.cuotaAlmacenamientoMb = cuotaAlmacenamientoMb;
    }

    public Boolean getEsAdministrador() {
        return esAdministrador;
    }

    public void setEsAdministrador(Boolean esAdministrador) {
        this.esAdministrador = esAdministrador;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}