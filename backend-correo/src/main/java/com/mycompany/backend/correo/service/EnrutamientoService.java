package com.mycompany.backend.correo.service;

import com.mycompany.backend.correo.dao.CarpetaDAO;
import com.mycompany.backend.correo.dao.EnrutamientoBuzonDAO;
import com.mycompany.backend.correo.model.EnrutamientoBuzon;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class EnrutamientoService {

    @Inject
    private EnrutamientoBuzonDAO enrutamientoDAO;

    @Inject
    private CarpetaDAO carpetaDAO;

    public List<EnrutamientoBuzon> listarPorDestinatario(UUID usuarioId) throws SQLException {
        return enrutamientoDAO.listarPorDestinatario(usuarioId);
    }

    public Optional<EnrutamientoBuzon> obtenerDeUsuario(UUID id, UUID usuarioId) throws SQLException {
        return enrutamientoDAO.obtenerPorId(id).filter(e -> usuarioId.equals(e.getDestinatarioId()));
    }

    // True si el usuario tiene un registro de buzón para ese mensaje (remitente o destinatario).
    public boolean tieneMensaje(UUID usuarioId, UUID mensajeId) throws SQLException {
        return enrutamientoDAO.listarPorDestinatario(usuarioId).stream()
                .anyMatch(e -> mensajeId.equals(e.getMensajeId()));
    }

    private void validarCarpeta(UUID carpetaId, UUID usuarioId) throws SQLException {
        carpetaDAO.obtenerPorId(carpetaId)
                .filter(c -> usuarioId.equals(c.getUsuarioId()))
                .orElseThrow(() -> new IllegalArgumentException("La carpeta no existe o no pertenece al usuario."));
    }

    public EnrutamientoBuzon crear(EnrutamientoBuzon nuevo, UUID usuarioId) throws SQLException {
        if (nuevo.getMensajeId() == null || nuevo.getCarpetaId() == null) {
            throw new IllegalArgumentException("mensajeId y carpetaId son obligatorios.");
        }
        validarCarpeta(nuevo.getCarpetaId(), usuarioId);
        nuevo.setDestinatarioId(usuarioId);
        return enrutamientoDAO.crear(nuevo);
    }

    public boolean actualizar(UUID id, UUID usuarioId, EnrutamientoBuzon datos) throws SQLException {
        if (obtenerDeUsuario(id, usuarioId).isEmpty()) {
            return false;
        }
        if (datos.getLeido() != null) {
            enrutamientoDAO.marcarComoLeido(id, usuarioId, datos.getLeido());
        }
        if (datos.getDestacado() != null) {
            enrutamientoDAO.marcarComoDestacado(id, usuarioId, datos.getDestacado());
        }
        if (datos.getCarpetaId() != null) {
            validarCarpeta(datos.getCarpetaId(), usuarioId);
            enrutamientoDAO.moverDeCarpeta(id, usuarioId, datos.getCarpetaId());
        }
        return true;
    }
}