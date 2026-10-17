package com.mycompany.backend.correo.service;

import com.mycompany.backend.correo.dao.CarpetaDAO;
import com.mycompany.backend.correo.dao.UsuarioDAO;
import com.mycompany.backend.correo.model.Carpeta;
import com.mycompany.backend.correo.model.Usuario;
import com.mycompany.backend.correo.security.PasswordUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UsuarioService {

    @Inject
    private UsuarioDAO usuarioDAO;

    @Inject
    private CarpetaDAO carpetaDAO;

    public Usuario registrarUsuario(Usuario usuario) throws SQLException {
        if (usuarioDAO.obtenerPorCorreo(usuario.getCorreo()).isPresent()) {
            throw new IllegalArgumentException("El correo electronico ya esta registrado.");
        }
        if (usuario.getContrasenaHash() == null || usuario.getContrasenaHash().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }

        // El campo llega con la contraseña en texto; aquí se guarda solo su hash.
        usuario.setContrasenaHash(PasswordUtil.hash(usuario.getContrasenaHash()));

        Usuario usuarioCreado = usuarioDAO.crear(usuario);

        String[] carpetasSistema = {"Entrada", "Enviados", "Borradores", "Spam", "Papelera", "Archivo"};
        for (String nombreCarpeta : carpetasSistema) {
            Carpeta carpeta = new Carpeta();
            carpeta.setUsuarioId(usuarioCreado.getId());
            carpeta.setNombre(nombreCarpeta);
            carpeta.setEsSistema(true);
            carpetaDAO.crear(carpeta);
        }

        return usuarioCreado;
    }

    public Optional<Usuario> obtenerPorId(UUID id) throws SQLException {
        return usuarioDAO.obtenerPorId(id);
    }

    public Optional<Usuario> obtenerPorCorreo(String correo) throws SQLException {
        return usuarioDAO.obtenerPorCorreo(correo);
    }

    public List<Usuario> listarUsuarios() throws SQLException {
        return usuarioDAO.listarTodos();
    }

    public boolean actualizarUsuario(Usuario usuario) throws SQLException {
        return usuarioDAO.actualizar(usuario);
    }

    public boolean cambiarEstado(UUID id, String nuevoEstado) throws SQLException {
        return usuarioDAO.cambiarEstado(id, nuevoEstado);
    }

    public boolean esAdministrador(UUID id) throws SQLException {
        return usuarioDAO.obtenerPorId(id)
                .map(u -> Boolean.TRUE.equals(u.getEsAdministrador()))
                .orElse(false);
    }

    public Usuario iniciarSesion(String correo, String contrasena) throws SQLException {
        Usuario usuario = usuarioDAO.obtenerPorCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Correo o contraseña incorrectos."));

        if (!PasswordUtil.verificar(contrasena, usuario.getContrasenaHash())) {
            throw new IllegalArgumentException("Correo o contraseña incorrectos.");
        }

        // Migración: cuenta antigua con contraseña en texto plano -> se guarda con hash.
        if (!PasswordUtil.esHashPbkdf2(usuario.getContrasenaHash())) {
            String nuevoHash = PasswordUtil.hash(contrasena);
            usuarioDAO.actualizarContrasena(usuario.getId(), nuevoHash);
            usuario.setContrasenaHash(nuevoHash);
        }

        if (!"activo".equals(usuario.getEstado())) {
            throw new IllegalArgumentException("La cuenta no está activa.");
        }
        return usuario;
    }
}