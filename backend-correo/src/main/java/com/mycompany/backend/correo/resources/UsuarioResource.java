package com.mycompany.backend.correo.resources;

import com.mycompany.backend.correo.model.Usuario;
import com.mycompany.backend.correo.service.RecuperacionService;
import com.mycompany.backend.correo.service.UsuarioService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    @Inject
    private UsuarioService usuarioService;

    @Inject
    private RecuperacionService recuperacionService;

    // Alta de usuarios: abierta (la usa el registro). No acepta es_administrador desde el cliente.
    @POST
    public Response registrarUsuario(Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
            return Response.status(Response.Status.CREATED).entity(publico(nuevoUsuario)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    // Solo administradores.
    @GET
    public Response listarUsuarios(@HeaderParam("X-Usuario-Id") String solicitanteId) {
        try {
            if (!esAdministrador(solicitanteId)) {
                return noAutorizado();
            }
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Usuario u : usuarioService.listarUsuarios()) {
                lista.add(publico(u));
            }
            return Response.ok(lista).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al consultar usuarios: " + e.getMessage()).build();
        }
    }

    // Solo administradores.
    @GET
    @Path("/{id}")
    public Response obtenerPorId(@PathParam("id") UUID id,
                                 @HeaderParam("X-Usuario-Id") String solicitanteId) {
        try {
            if (!esAdministrador(solicitanteId)) {
                return noAutorizado();
            }
            Optional<Usuario> usuarioOpt = usuarioService.obtenerPorId(id);
            if (usuarioOpt.isPresent()) {
                return Response.ok(publico(usuarioOpt.get())).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado.").build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    // Solo administradores.
    @PUT
    @Path("/{id}")
    public Response actualizarUsuario(@PathParam("id") UUID id,
                                      @HeaderParam("X-Usuario-Id") String solicitanteId,
                                      Usuario datos) {
        try {
            if (!esAdministrador(solicitanteId)) {
                return noAutorizado();
            }
            Optional<Usuario> opcional = usuarioService.obtenerPorId(id);
            if (opcional.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado.").build();
            }
            Usuario usuario = opcional.get();
            if (datos.getNombreCompleto() != null && !datos.getNombreCompleto().isBlank()) {
                usuario.setNombreCompleto(datos.getNombreCompleto());
            }
            if (datos.getEstado() != null) {
                if (!List.of("activo", "suspendido", "eliminado").contains(datos.getEstado())) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Estado no válido.").build();
                }
                usuario.setEstado(datos.getEstado());
            }
            if (datos.getCuotaAlmacenamientoMb() != null) {
                if (datos.getCuotaAlmacenamientoMb() <= 0) {
                    return Response.status(Response.Status.BAD_REQUEST).entity("La cuota debe ser mayor a 0.").build();
                }
                usuario.setCuotaAlmacenamientoMb(datos.getCuotaAlmacenamientoMb());
            }
            usuarioService.actualizarUsuario(usuario);
            Usuario actualizado = usuarioService.obtenerPorId(id).orElse(usuario);
            return Response.ok(publico(actualizado)).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    // Solo administradores. Borrado lógico: cambia el estado a "eliminado".
    @DELETE
    @Path("/{id}")
    public Response eliminarUsuario(@PathParam("id") UUID id,
                                    @HeaderParam("X-Usuario-Id") String solicitanteId) {
        try {
            if (!esAdministrador(solicitanteId)) {
                return noAutorizado();
            }
            if (id.toString().equalsIgnoreCase(solicitanteId.trim())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("No puedes eliminar tu propia cuenta.").build();
            }
            if (usuarioService.cambiarEstado(id, "eliminado")) {
                return Response.noContent().build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado.").build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    // Abierto: inicio de sesión.
    @POST
    @Path("/login")
    public Response iniciarSesion(Map<String, String> credenciales) {
        try {
            Usuario usuario = usuarioService.iniciarSesion(
                    credenciales.get("correo"), credenciales.get("contrasena"));
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("id", usuario.getId().toString());
            datos.put("correo", usuario.getCorreo());
            datos.put("nombreCompleto", usuario.getNombreCompleto());
            datos.put("esAdministrador", Boolean.TRUE.equals(usuario.getEsAdministrador()));
            return Response.ok(datos).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    // Abierto: pide un enlace de recuperación. La respuesta es igual exista o no el correo.
    @POST
    @Path("/recuperar")
    public Response solicitarRecuperacion(Map<String, String> datos) {
        try {
            recuperacionService.solicitar(datos == null ? null : datos.get("correo"));
            return Response.ok(Map.of("mensaje",
                    "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña.")).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    // Abierto: cambia la contraseña con un token válido de un solo uso.
    @POST
    @Path("/restablecer")
    public Response restablecerContrasena(Map<String, String> datos) {
        try {
            recuperacionService.restablecer(
                    datos == null ? null : datos.get("token"),
                    datos == null ? null : datos.get("contrasena"));
            return Response.ok(Map.of("mensaje", "Contraseña actualizada. Ya puedes iniciar sesión.")).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    // Respuesta pública de un usuario: nunca incluye contrasenaHash.
    private static Map<String, Object> publico(Usuario u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId() == null ? null : u.getId().toString());
        m.put("correo", u.getCorreo());
        m.put("nombreCompleto", u.getNombreCompleto());
        m.put("estado", u.getEstado());
        m.put("cuotaAlmacenamientoMb", u.getCuotaAlmacenamientoMb());
        m.put("esAdministrador", Boolean.TRUE.equals(u.getEsAdministrador()));
        return m;
    }

    private boolean esAdministrador(String solicitanteId) throws SQLException {
        if (solicitanteId == null || solicitanteId.isBlank()) {
            return false;
        }
        try {
            return usuarioService.esAdministrador(UUID.fromString(solicitanteId.trim()));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static Response noAutorizado() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity("Se requiere una cuenta de administrador.").build();
    }
}