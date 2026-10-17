package com.mycompany.backend.correo.dao;

import com.mycompany.backend.correo.DatabaseConnection;
import com.mycompany.backend.correo.model.Adjunto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AdjuntoDAO {

    @Inject
    private DatabaseConnection databaseConnection;

    public Adjunto crear(Adjunto adjunto) throws SQLException {
        String sql = "INSERT INTO adjuntos (mensaje_id, nombre_archivo, tipo_mime, tamano_bytes, ruta_servidor) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "RETURNING id, fecha_subida";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, adjunto.getMensajeId());
            stmt.setString(2, adjunto.getNombreArchivo());
            stmt.setString(3, adjunto.getTipoMime());
            stmt.setLong(4, adjunto.getTamanoBytes());
            stmt.setString(5, adjunto.getRutaServidor());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    adjunto.setId(rs.getObject("id", UUID.class));
                    adjunto.setFechaSubida(rs.getObject("fecha_subida", OffsetDateTime.class));
                }
            }
        }
        return adjunto;
    }

    public Optional<Adjunto> obtenerPorId(UUID id) throws SQLException {
        String sql = "SELECT id, mensaje_id, nombre_archivo, tipo_mime, tamano_bytes, ruta_servidor, fecha_subida " +
                     "FROM adjuntos WHERE id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearAdjunto(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Adjunto> listarPorMensaje(UUID mensajeId) throws SQLException {
        List<Adjunto> adjuntos = new ArrayList<>();
        String sql = "SELECT id, mensaje_id, nombre_archivo, tipo_mime, tamano_bytes, ruta_servidor, fecha_subida " +
                     "FROM adjuntos WHERE mensaje_id = ? ORDER BY fecha_subida ASC";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, mensajeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    adjuntos.add(mapearAdjunto(rs));
                }
            }
        }
        return adjuntos;
    }

    public boolean eliminar(UUID id) throws SQLException {
        String sql = "DELETE FROM adjuntos WHERE id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    
    public void moverAMensaje(UUID origenMensajeId, UUID destinoMensajeId) throws SQLException {
    String sql = "UPDATE adjuntos SET mensaje_id = ? WHERE mensaje_id = ?";

    try (Connection conn = databaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setObject(1, destinoMensajeId);
        stmt.setObject(2, origenMensajeId);
        stmt.executeUpdate();
    }
}
    
    
    public long usoAlmacenamientoBytes(UUID usuarioId) throws SQLException {
    String sql = "SELECT COALESCE(SUM(a.tamano_bytes), 0) FROM adjuntos a " +
                 "JOIN mensajes m ON m.id = a.mensaje_id WHERE m.remitente_id = ?";

    try (Connection conn = databaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setObject(1, usuarioId);
        try (ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }
}

    private Adjunto mapearAdjunto(ResultSet rs) throws SQLException {
        Adjunto adjunto = new Adjunto();
        adjunto.setId(rs.getObject("id", UUID.class));
        adjunto.setMensajeId(rs.getObject("mensaje_id", UUID.class));
        adjunto.setNombreArchivo(rs.getString("nombre_archivo"));
        adjunto.setTipoMime(rs.getString("tipo_mime"));
        adjunto.setTamanoBytes(rs.getLong("tamano_bytes"));
        adjunto.setRutaServidor(rs.getString("ruta_servidor"));
        adjunto.setFechaSubida(rs.getObject("fecha_subida", OffsetDateTime.class));
        return adjunto;
    }
    
        public List<Adjunto> listarPorRemitente(UUID remitenteId) throws SQLException {
        List<Adjunto> adjuntos = new ArrayList<>();
        String sql = "SELECT a.id, a.mensaje_id, a.nombre_archivo, a.tipo_mime, a.tamano_bytes, a.ruta_servidor, a.fecha_subida " +
                     "FROM adjuntos a JOIN mensajes m ON m.id = a.mensaje_id " +
                     "WHERE m.remitente_id = ? ORDER BY a.fecha_subida DESC";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, remitenteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    adjuntos.add(mapearAdjunto(rs));
                }
            }
        }
        return adjuntos;
    }
}