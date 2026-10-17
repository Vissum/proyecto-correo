package com.mycompany.backend.correo.dao;

import com.mycompany.backend.correo.DatabaseConnection;
import com.mycompany.backend.correo.model.Carpeta;
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
public class CarpetaDAO {

    @Inject
    private DatabaseConnection databaseConnection;

    public Carpeta crear(Carpeta carpeta) throws SQLException {
        String sql = "INSERT INTO carpetas (usuario_id, nombre, es_sistema) " +
                     "VALUES (?, ?, COALESCE(?, false)) " +
                     "RETURNING id, fecha_creacion";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, carpeta.getUsuarioId());
            stmt.setString(2, carpeta.getNombre());
            if (carpeta.getEsSistema() != null) {
                stmt.setBoolean(3, carpeta.getEsSistema());
            } else {
                stmt.setNull(3, java.sql.Types.BOOLEAN);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    carpeta.setId(rs.getObject("id", UUID.class));
                    carpeta.setFechaCreacion(rs.getObject("fecha_creacion", OffsetDateTime.class));
                }
            }
        }
        return carpeta;
    }

    public Optional<Carpeta> obtenerPorId(UUID id) throws SQLException {
        String sql = "SELECT id, usuario_id, nombre, es_sistema, fecha_creacion " +
                     "FROM carpetas WHERE id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearCarpeta(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Carpeta> listarPorUsuario(UUID usuarioId) throws SQLException {
        List<Carpeta> carpetas = new ArrayList<>();
        String sql = "SELECT id, usuario_id, nombre, es_sistema, fecha_creacion " +
                     "FROM carpetas WHERE usuario_id = ? ORDER BY es_sistema DESC, nombre ASC";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, usuarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    carpetas.add(mapearCarpeta(rs));
                }
            }
        }
        return carpetas;
    }

    public Optional<Carpeta> obtenerPorUsuarioYNombre(UUID usuarioId, String nombre) throws SQLException {
        String sql = "SELECT id, usuario_id, nombre, es_sistema, fecha_creacion " +
                     "FROM carpetas WHERE usuario_id = ? AND nombre = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, usuarioId);
            stmt.setString(2, nombre);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearCarpeta(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean actualizarNombre(UUID id, UUID usuarioId, String nuevoNombre) throws SQLException {
        String sql = "UPDATE carpetas SET nombre = ? WHERE id = ? AND usuario_id = ? AND es_sistema = false";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoNombre);
            stmt.setObject(2, id);
            stmt.setObject(3, usuarioId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(UUID id, UUID usuarioId) throws SQLException {
        String sql = "DELETE FROM carpetas WHERE id = ? AND usuario_id = ? AND es_sistema = false";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            stmt.setObject(2, usuarioId);

            return stmt.executeUpdate() > 0;
        }
    }

    private Carpeta mapearCarpeta(ResultSet rs) throws SQLException {
        Carpeta carpeta = new Carpeta();
        carpeta.setId(rs.getObject("id", UUID.class));
        carpeta.setUsuarioId(rs.getObject("usuario_id", UUID.class));
        carpeta.setNombre(rs.getString("nombre"));
        carpeta.setEsSistema(rs.getBoolean("es_sistema"));
        carpeta.setFechaCreacion(rs.getObject("fecha_creacion", OffsetDateTime.class));
        return carpeta;
    }
}