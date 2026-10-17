package com.mycompany.backend.correo.dao;

import com.mycompany.backend.correo.DatabaseConnection;
import com.mycompany.backend.correo.model.EnrutamientoBuzon;
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
public class EnrutamientoBuzonDAO {

    @Inject
    private DatabaseConnection databaseConnection;

    public EnrutamientoBuzon crear(EnrutamientoBuzon enrutamiento) throws SQLException {
        String sql = "INSERT INTO enrutamiento_buzones (mensaje_id, destinatario_id, carpeta_id, tipo_envio, leido, destacado) " +
                     "VALUES (?, ?, ?, COALESCE(?, 'para'), COALESCE(?, false), COALESCE(?, false)) " +
                     "RETURNING id, fecha_recepcion";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, enrutamiento.getMensajeId());
            stmt.setObject(2, enrutamiento.getDestinatarioId());
            stmt.setObject(3, enrutamiento.getCarpetaId());
            stmt.setString(4, enrutamiento.getTipoEnvio());
            if (enrutamiento.getLeido() != null) {
                stmt.setBoolean(5, enrutamiento.getLeido());
            } else {
                stmt.setNull(5, java.sql.Types.BOOLEAN);
            }
            if (enrutamiento.getDestacado() != null) {
                stmt.setBoolean(6, enrutamiento.getDestacado());
            } else {
                stmt.setNull(6, java.sql.Types.BOOLEAN);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    enrutamiento.setId(rs.getObject("id", UUID.class));
                    enrutamiento.setFechaRecepcion(rs.getObject("fecha_recepcion", OffsetDateTime.class));
                }
            }
        }
        return enrutamiento;
    }

    public Optional<EnrutamientoBuzon> obtenerPorId(UUID id) throws SQLException {
        String sql = "SELECT id, mensaje_id, destinatario_id, carpeta_id, tipo_envio, leido, destacado, fecha_recepcion " +
                     "FROM enrutamiento_buzones WHERE id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearEnrutamientoBuzon(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<EnrutamientoBuzon> listarPorCarpeta(UUID destinatarioId, UUID carpetaId) throws SQLException {
        List<EnrutamientoBuzon> enrutamientos = new ArrayList<>();
        String sql = "SELECT id, mensaje_id, destinatario_id, carpeta_id, tipo_envio, leido, destacado, fecha_recepcion " +
                     "FROM enrutamiento_buzones " +
                     "WHERE destinatario_id = ? AND carpeta_id = ? " +
                     "ORDER BY fecha_recepcion DESC";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, destinatarioId);
            stmt.setObject(2, carpetaId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrutamientos.add(mapearEnrutamientoBuzon(rs));
                }
            }
        }
        return enrutamientos;
    }

    public boolean marcarComoLeido(UUID id, UUID destinatarioId, boolean leido) throws SQLException {
        String sql = "UPDATE enrutamiento_buzones SET leido = ? WHERE id = ? AND destinatario_id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, leido);
            stmt.setObject(2, id);
            stmt.setObject(3, destinatarioId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean marcarComoDestacado(UUID id, UUID destinatarioId, boolean destacado) throws SQLException {
        String sql = "UPDATE enrutamiento_buzones SET destacado = ? WHERE id = ? AND destinatario_id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, destacado);
            stmt.setObject(2, id);
            stmt.setObject(3, destinatarioId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean moverDeCarpeta(UUID id, UUID destinatarioId, UUID nuevaCarpetaId) throws SQLException {
        String sql = "UPDATE enrutamiento_buzones SET carpeta_id = ? WHERE id = ? AND destinatario_id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, nuevaCarpetaId);
            stmt.setObject(2, id);
            stmt.setObject(3, destinatarioId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(UUID id, UUID destinatarioId) throws SQLException {
        String sql = "DELETE FROM enrutamiento_buzones WHERE id = ? AND destinatario_id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            stmt.setObject(2, destinatarioId);

            return stmt.executeUpdate() > 0;
        }
    }

    public List<EnrutamientoBuzon> buscarPorTexto(UUID destinatarioId, String texto) throws SQLException {
        List<EnrutamientoBuzon> enrutamientos = new ArrayList<>();
        String sql = "SELECT e.id, e.mensaje_id, e.destinatario_id, e.carpeta_id, e.tipo_envio, e.leido, e.destacado, e.fecha_recepcion " +
                     "FROM enrutamiento_buzones e " +
                     "JOIN mensajes m ON m.id = e.mensaje_id " +
                     "WHERE e.destinatario_id = ? AND m.busqueda @@ plainto_tsquery('spanish', ?) " +
                     "ORDER BY e.fecha_recepcion DESC";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, destinatarioId);
            stmt.setString(2, texto);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enrutamientos.add(mapearEnrutamientoBuzon(rs));
                }
            }
        }
        return enrutamientos;
    }

    private EnrutamientoBuzon mapearEnrutamientoBuzon(ResultSet rs) throws SQLException {
        EnrutamientoBuzon enrutamiento = new EnrutamientoBuzon();
        enrutamiento.setId(rs.getObject("id", UUID.class));
        enrutamiento.setMensajeId(rs.getObject("mensaje_id", UUID.class));
        enrutamiento.setDestinatarioId(rs.getObject("destinatario_id", UUID.class));
        enrutamiento.setCarpetaId(rs.getObject("carpeta_id", UUID.class));
        enrutamiento.setTipoEnvio(rs.getString("tipo_envio"));
        enrutamiento.setLeido(rs.getBoolean("leido"));
        enrutamiento.setDestacado(rs.getBoolean("destacado"));
        enrutamiento.setFechaRecepcion(rs.getObject("fecha_recepcion", OffsetDateTime.class));
        return enrutamiento;
    }
    
    
        public List<EnrutamientoBuzon> listarPorDestinatario(UUID destinatarioId) throws SQLException {
        List<EnrutamientoBuzon> lista = new ArrayList<>();
        String sql = "SELECT id, mensaje_id, destinatario_id, carpeta_id, tipo_envio, leido, destacado, fecha_recepcion " +
                     "FROM enrutamiento_buzones WHERE destinatario_id = ? ORDER BY fecha_recepcion DESC";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, destinatarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearEnrutamientoBuzon(rs));
                }
            }
        }
        return lista;
    }
}