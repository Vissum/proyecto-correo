package com.mycompany.backend.correo.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Mensaje {

    private UUID id;
    private UUID remitenteId;
    private String asunto;
    private String cuerpoPlano;
    private String cuerpoHtml;
    private String paisOrigen;
    private String ipOrigen;
    private OffsetDateTime fechaEnvio;
    private String borradorPara;
    private String borradorCc;
    private String borradorCco;

    public Mensaje() {
    }

    public Mensaje(UUID id, UUID remitenteId, String asunto, String cuerpoPlano, String cuerpoHtml, String paisOrigen, String ipOrigen, OffsetDateTime fechaEnvio) {
        this.id = id;
        this.remitenteId = remitenteId;
        this.asunto = asunto;
        this.cuerpoPlano = cuerpoPlano;
        this.cuerpoHtml = cuerpoHtml;
        this.paisOrigen = paisOrigen;
        this.ipOrigen = ipOrigen;
        this.fechaEnvio = fechaEnvio;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRemitenteId() {
        return remitenteId;
    }

    public void setRemitenteId(UUID remitenteId) {
        this.remitenteId = remitenteId;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getCuerpoPlano() {
        return cuerpoPlano;
    }

    public void setCuerpoPlano(String cuerpoPlano) {
        this.cuerpoPlano = cuerpoPlano;
    }

    public String getCuerpoHtml() {
        return cuerpoHtml;
    }

    public void setCuerpoHtml(String cuerpoHtml) {
        this.cuerpoHtml = cuerpoHtml;
    }

    public String getPaisOrigen() {
        return paisOrigen;
    }

    public void setPaisOrigen(String paisOrigen) {
        this.paisOrigen = paisOrigen;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public void setIpOrigen(String ipOrigen) {
        this.ipOrigen = ipOrigen;
    }

    public OffsetDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(OffsetDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getBorradorPara() {
        return borradorPara;
    }

    public void setBorradorPara(String borradorPara) {
        this.borradorPara = borradorPara;
    }

    public String getBorradorCc() {
        return borradorCc;
    }

    public void setBorradorCc(String borradorCc) {
        this.borradorCc = borradorCc;
    }

    public String getBorradorCco() {
        return borradorCco;
    }

    public void setBorradorCco(String borradorCco) {
        this.borradorCco = borradorCco;
    }
}