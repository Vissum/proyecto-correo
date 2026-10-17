package com.mycompany.backend.correo.dao;

import com.mycompany.backend.correo.DatabaseConnection;
import com.mycompany.backend.correo.model.Mensaje;
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
public class MensajeDAO {

    private static final String COLUMNAS =
            "id, remitente_id, asunto, cuerpo_plano, cuerpo_html, pais_origen, " +
            "HOST(ip_origen) AS ip_origen, fecha_envio, borrador_para, borrador_cc, borrador_cco";

    @Inject
    private DatabaseConnection databaseConnection;

    public Mensaje crear(Mensaje mensaje) throws SQLException {
        String sql = "INSERT INTO mensajes (remitente_id, asunto, cuerpo_plano, cuerpo_html, pais_origen, ip_origen, " +
                     "borrador_para, borrador_cc, borrador_cco) " +
                     "VALUES (?, ?, ?, ?, ?, CAST(? AS inet), ?, ?, ?) " +
                     "RETURNING id, fecha_envio";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, mensaje.getRemitenteId());
            stmt.setString(2, mensaje.getAsunto());
            stmt.setString(3, mensaje.getCuerpoPlano());
            stmt.setString(4, mensaje.getCuerpoHtml());
            stmt.setString(5, mensaje.getPaisOrigen());
            stmt.setString(6, mensaje.getIpOrigen());
            stmt.setString(7, mensaje.getBorradorPara());
            stmt.setString(8, mensaje.getBorradorCc());
            stmt.setString(9, mensaje.getBorradorCco());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    mensaje.setId(rs.getObject("id", UUID.class));
                    mensaje.setFechaEnvio(rs.getObject("fecha_envio", OffsetDateTime.class));
                }
            }
        }
        return mensaje;
    }

    public Optional<Mensaje> obtenerPorId(UUID id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM mensajes WHERE id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearMensaje(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Mensaje> listarPorRemitente(UUID remitenteId) throws SQLException {
        List<Mensaje> mensajes = new ArrayList<>();
        String sql = "SELECT " + COLUMNAS + " FROM mensajes WHERE remitente_id = ? ORDER BY fecha_envio DESC";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, remitenteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    mensajes.add(mapearMensaje(rs));
                }
            }
        }
        return mensajes;
    }

    // Actualiza un borrador existente, solo si pertenece al remitente indicado
    public boolean actualizarBorrador(Mensaje mensaje) throws SQLException {
        String sql = "UPDATE mensajes SET asunto = ?, cuerpo_plano = ?, " +
                     "borrador_para = ?, borrador_cc = ?, borrador_cco = ? " +
                     "WHERE id = ? AND remitente_id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, mensaje.getAsunto());
            stmt.setString(2, mensaje.getCuerpoPlano());
            stmt.setString(3, mensaje.getBorradorPara());
            stmt.setString(4, mensaje.getBorradorCc());
            stmt.setString(5, mensaje.getBorradorCco());
            stmt.setObject(6, mensaje.getId());
            stmt.setObject(7, mensaje.getRemitenteId());

            return stmt.executeUpdate() > 0;
        }
    }

    // Elimina el mensaje; sus enrutamientos y adjuntos se borran en cascada
    public boolean eliminar(UUID id) throws SQLException {
        String sql = "DELETE FROM mensajes WHERE id = ?";

        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private Mensaje mapearMensaje(ResultSet rs) throws SQLException {
        Mensaje mensaje = new Mensaje();
        mensaje.setId(rs.getObject("id", UUID.class));
        mensaje.setRemitenteId(rs.getObject("remitente_id", UUID.class));
        mensaje.setAsunto(rs.getString("asunto"));
        mensaje.setCuerpoPlano(rs.getString("cuerpo_plano"));
        mensaje.setCuerpoHtml(rs.getString("cuerpo_html"));
        mensaje.setPaisOrigen(rs.getString("pais_origen"));
        mensaje.setIpOrigen(rs.getString("ip_origen"));
        mensaje.setFechaEnvio(rs.getObject("fecha_envio", OffsetDateTime.class));
        mensaje.setBorradorPara(rs.getString("borrador_para"));
        mensaje.setBorradorCc(rs.getString("borrador_cc"));
        mensaje.setBorradorCco(rs.getString("borrador_cco"));
        return mensaje;
    }
}