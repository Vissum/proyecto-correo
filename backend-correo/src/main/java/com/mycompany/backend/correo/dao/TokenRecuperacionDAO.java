package com.mycompany.backend.correo.dao;

import com.mycompany.backend.correo.DatabaseConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TokenRecuperacionDAO {

    @Inject
    private DatabaseConnection databaseConnection;

    public void crear(UUID usuarioId, String tokenHash, OffsetDateTime expiraEn) throws SQLException {
        String sql = "INSERT INTO tokens_recuperacion (usuario_id, token_hash, expira_en) VALUES (?, ?, ?)";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, usuarioId);
            stmt.setString(2, tokenHash);
            stmt.setObject(3, expiraEn);
            stmt.executeUpdate();
        }
    }

    // Invalida los enlaces anteriores del usuario al pedir uno nuevo.
    public void invalidarPendientes(UUID usuarioId) throws SQLException {
        String sql = "UPDATE tokens_recuperacion SET usado = true WHERE usuario_id = ? AND usado = false";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, usuarioId);
            stmt.executeUpdate();
        }
    }

    // Consumo atómico: marca el token como usado solo si sigue vigente y sin usar.
    public Optional<UUID> consumir(String tokenHash) throws SQLException {
        String sql = "UPDATE tokens_recuperacion SET usado = true " +
                     "WHERE token_hash = ? AND usado = false AND expira_en > now() " +
                     "RETURNING usuario_id";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tokenHash);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getObject("usuario_id", UUID.class));
                }
            }
        }
        return Optional.empty();
    }
}