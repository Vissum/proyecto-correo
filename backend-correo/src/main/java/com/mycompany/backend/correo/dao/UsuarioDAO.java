package com.mycompany.backend.correo.dao;

import com.mycompany.backend.correo.DatabaseConnection;
import com.mycompany.backend.correo.model.Usuario;
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
public class UsuarioDAO {

    private static final String COLUMNAS =
            "id, correo, contrasena_hash, nombre_completo, estado, cuota_almacenamiento_mb, es_administrador, fecha_creacion";

    @Inject
    private DatabaseConnection databaseConnection;

    // El alta nunca toma es_administrador del cliente: siempre queda en false (default de la BD).
    public Usuario crear(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (correo, contrasena_hash, nombre_completo, estado, cuota_almacenamiento_mb) " +
                     "VALUES (?, ?, ?, COALESCE(?, 'activo'), COALESCE(?, 1024)) " +
                     "RETURNING id, fecha_creacion";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getCorreo());
            stmt.setString(2, usuario.getContrasenaHash());
            stmt.setString(3, usuario.getNombreCompleto());
            stmt.setString(4, usuario.getEstado());
            if (usuario.getCuotaAlmacenamientoMb() != null) {
                stmt.setInt(5, usuario.getCuotaAlmacenamientoMb());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    usuario.setId(rs.getObject("id", UUID.class));
                    usuario.setFechaCreacion(rs.getObject("fecha_creacion", OffsetDateTime.class));
                }
            }
        }
        usuario.setEsAdministrador(false);
        return usuario;
    }

    public Optional<Usuario> obtenerPorId(UUID id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM usuarios WHERE id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Usuario> obtenerPorCorreo(String correo) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM usuarios WHERE correo = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, correo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM usuarios ORDER BY fecha_creacion DESC";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }

    public boolean actualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nombre_completo = ?, estado = ?, cuota_almacenamiento_mb = ? " +
                     "WHERE id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNombreCompleto());
            stmt.setString(2, usuario.getEstado());
            stmt.setInt(3, usuario.getCuotaAlmacenamientoMb());
            stmt.setObject(4, usuario.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean cambiarEstado(UUID id, String nuevoEstado) throws SQLException {
        String sql = "UPDATE usuarios SET estado = ? WHERE id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoEstado);
            stmt.setObject(2, id);

            return stmt.executeUpdate() > 0;
        }
    }

    // Guarda un hash ya calculado. Nunca recibe texto plano.
    public boolean actualizarContrasena(UUID id, String contrasenaHash) throws SQLException {
        String sql = "UPDATE usuarios SET contrasena_hash = ? WHERE id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, contrasenaHash);
            stmt.setObject(2, id);

            return stmt.executeUpdate() > 0;
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getObject("id", UUID.class));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setContrasenaHash(rs.getString("contrasena_hash"));
        usuario.setNombreCompleto(rs.getString("nombre_completo"));
        usuario.setEstado(rs.getString("estado"));
        usuario.setCuotaAlmacenamientoMb(rs.getInt("cuota_almacenamiento_mb"));
        usuario.setEsAdministrador(rs.getBoolean("es_administrador"));
        usuario.setFechaCreacion(rs.getObject("fecha_creacion", OffsetDateTime.class));
        return usuario;
    }
}