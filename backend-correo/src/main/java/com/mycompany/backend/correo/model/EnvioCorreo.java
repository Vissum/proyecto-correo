package com.mycompany.backend.correo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EnvioCorreo {

    private UUID remitenteId;
    private UUID mensajeId;
    private UUID borradorMensajeId;
    private String asunto;
    private String cuerpoPlano;
    private String paisOrigen;
    private List<String> para = new ArrayList<>();
    private List<String> cc = new ArrayList<>();
    private List<String> cco = new ArrayList<>();

    public UUID getRemitenteId() { return remitenteId; }
    public void setRemitenteId(UUID remitenteId) { this.remitenteId = remitenteId; }
    public UUID getMensajeId() { return mensajeId; }
    public void setMensajeId(UUID mensajeId) { this.mensajeId = mensajeId; }
    public UUID getBorradorMensajeId() { return borradorMensajeId; }
    public void setBorradorMensajeId(UUID borradorMensajeId) { this.borradorMensajeId = borradorMensajeId; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getCuerpoPlano() { return cuerpoPlano; }
    public void setCuerpoPlano(String cuerpoPlano) { this.cuerpoPlano = cuerpoPlano; }
    public String getPaisOrigen() { return paisOrigen; }
    public void setPaisOrigen(String paisOrigen) { this.paisOrigen = paisOrigen; }
    public List<String> getPara() { return para; }
    public void setPara(List<String> para) { this.para = para != null ? para : new ArrayList<>(); }
    public List<String> getCc() { return cc; }
    public void setCc(List<String> cc) { this.cc = cc != null ? cc : new ArrayList<>(); }
    public List<String> getCco() { return cco; }
    public void setCco(List<String> cco) { this.cco = cco != null ? cco : new ArrayList<>(); }
}