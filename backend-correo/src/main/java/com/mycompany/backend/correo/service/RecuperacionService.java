package com.mycompany.backend.correo.service;

import com.mycompany.backend.correo.dao.TokenRecuperacionDAO;
import com.mycompany.backend.correo.dao.UsuarioDAO;
import com.mycompany.backend.correo.model.Usuario;
import com.mycompany.backend.correo.security.PasswordUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class RecuperacionService {

    private static final Logger LOG = Logger.getLogger(RecuperacionService.class.getName());
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MINUTOS_VIGENCIA = 30;

    @Inject
    private UsuarioDAO usuarioDAO;

    @Inject
    private TokenRecuperacionDAO tokenDAO;

    @Inject
    private MailService mailService;

    // Siempre termina igual para el cliente: no revela si el correo existe.
    public void solicitar(String correo) throws SQLException {
        if (correo == null || correo.isBlank()) {
            return;
        }
        Optional<Usuario> opcional = usuarioDAO.obtenerPorCorreo(correo.trim());
        if (opcional.isEmpty() || !"activo".equals(opcional.get().getEstado())) {
            return;
        }
        Usuario usuario = opcional.get();

        tokenDAO.invalidarPendientes(usuario.getId());

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokenDAO.crear(usuario.getId(), sha256(token), OffsetDateTime.now().plusMinutes(MINUTOS_VIGENCIA));

        String enlace = MailService.urlFrontend() + "/restablecer?token=" + token;
        try {
            mailService.enviarRecuperacion(usuario.getCorreo(), usuario.getNombreCompleto(), enlace);
        } catch (IllegalStateException e) {
            LOG.log(Level.SEVERE, "Falla al enviar correo de recuperación", e);
        }
    }

    public void restablecer(String token, String nuevaContrasena) throws SQLException {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El enlace no es válido.");
        }
        if (nuevaContrasena == null || nuevaContrasena.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        Optional<UUID> usuarioId = tokenDAO.consumir(sha256(token.trim()));
        if (usuarioId.isEmpty()) {
            throw new IllegalArgumentException("El enlace no es válido, ya se usó o venció.");
        }
        usuarioDAO.actualizarContrasena(usuarioId.get(), PasswordUtil.hash(nuevaContrasena));
    }

    private static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}