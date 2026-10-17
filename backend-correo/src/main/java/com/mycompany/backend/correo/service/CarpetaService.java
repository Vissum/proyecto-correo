package com.mycompany.backend.correo.service;

import com.mycompany.backend.correo.dao.CarpetaDAO;
import com.mycompany.backend.correo.model.Carpeta;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import java.util.Optional;

@ApplicationScoped
public class CarpetaService {

    @Inject
    private CarpetaDAO carpetaDAO;

    public Carpeta crearCarpeta(UUID usuarioId, String nombre) throws SQLException {
        if (carpetaDAO.obtenerPorUsuarioYNombre(usuarioId, nombre).isPresent()) {
            throw new IllegalArgumentException("Ya existe una carpeta con ese nombre.");
        }

        Carpeta carpeta = new Carpeta();
        carpeta.setUsuarioId(usuarioId);
        carpeta.setNombre(nombre);
        carpeta.setEsSistema(false);

        return carpetaDAO.crear(carpeta);
    }

    public List<Carpeta> listarPorUsuario(UUID usuarioId) throws SQLException {
        return carpetaDAO.listarPorUsuario(usuarioId);
    }

    public boolean renombrarCarpeta(UUID id, UUID usuarioId, String nuevoNombre) throws SQLException {
        if (carpetaDAO.obtenerPorUsuarioYNombre(usuarioId, nuevoNombre).isPresent()) {
            throw new IllegalArgumentException("Ya existe una carpeta con ese nombre.");
        }
        return carpetaDAO.actualizarNombre(id, usuarioId, nuevoNombre);
    }

    public boolean eliminarCarpeta(UUID id, UUID usuarioId) throws SQLException {
        return carpetaDAO.eliminar(id, usuarioId);
    }
    
        public Optional<Carpeta> obtenerPorId(UUID id, UUID usuarioId) throws SQLException {
        return carpetaDAO.obtenerPorId(id).filter(c -> usuarioId.equals(c.getUsuarioId()));
    }
}