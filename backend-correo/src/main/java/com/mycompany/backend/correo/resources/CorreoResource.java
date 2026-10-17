package com.mycompany.backend.correo.resources;

import com.mycompany.backend.correo.model.Adjunto;
import com.mycompany.backend.correo.model.EnrutamientoBuzon;
import com.mycompany.backend.correo.model.EnvioCorreo;
import com.mycompany.backend.correo.model.Mensaje;
import com.mycompany.backend.correo.security.Autorizacion;
import com.mycompany.backend.correo.service.AdjuntoService;
import com.mycompany.backend.correo.service.CorreoService;
import com.mycompany.backend.correo.service.EnrutamientoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Path("/correos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CorreoResource {

    @Inject
    private CorreoService correoService;

    @Inject
    private AdjuntoService adjuntoService;

    @Inject
    private EnrutamientoService enrutamientoService;

    @POST
    @Path("/enviar")
    public Response enviarCorreo(
            @HeaderParam("X-Usuario-Id") String solicitante,
            @QueryParam("correoDestinatario") String correoDestinatario,
            @QueryParam("tipoEnvio") @DefaultValue("para") String tipoEnvio,
            Mensaje mensaje) {

        if (mensaje == null || !Autorizacion.esMismoUsuario(solicitante, mensaje.getRemitenteId())) {
            return noAutorizado();
        }
        if (correoDestinatario == null || correoDestinatario.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El correo del destinatario es obligatorio.").build();
        }
        try {
            Mensaje mensajeEnviado = correoService.enviarCorreo(mensaje, correoDestinatario, tipoEnvio);
            return Response.status(Response.Status.CREATED).entity(mensajeEnviado).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (SQLException | IllegalStateException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al procesar el envio: " + e.getMessage()).build();
        }
    }

    @POST
    @Path("/enviar-multiple")
    public Response enviarACorreos(@HeaderParam("X-Usuario-Id") String solicitante, EnvioCorreo envio) {
        if (envio == null || !Autorizacion.esMismoUsuario(solicitante, envio.getRemitenteId())) {
            return noAutorizado();
        }
        try {
            Mensaje enviado = correoService.enviarCorreoMultiple(envio);
            return Response.status(Response.Status.CREATED).entity(enviado).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (SQLException | IllegalStateException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al procesar el envio: " + e.getMessage()).build();
        }
    }

    @POST
    @Path("/borradores")
    public Response guardarBorrador(@HeaderParam("X-Usuario-Id") String solicitante, EnvioCorreo envio) {
        if (envio == null || !Autorizacion.esMismoUsuario(solicitante, envio.getRemitenteId())) {
            return noAutorizado();
        }
        try {
            Mensaje guardado = correoService.guardarBorrador(envio);
            return Response.status(Response.Status.CREATED).entity(guardado).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (SQLException | IllegalStateException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al guardar el borrador: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/buzon")
    public Response obtenerBuzon(
            @HeaderParam("X-Usuario-Id") String solicitante,
            @QueryParam("usuarioId") UUID usuarioId,
            @QueryParam("carpetaId") UUID carpetaId) {

        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        if (carpetaId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parametro carpetaId es obligatorio.").build();
        }
        try {
            List<EnrutamientoBuzon> buzon = correoService.obtenerBuzonPorCarpeta(usuarioId, carpetaId);
            return Response.ok(buzon).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al consultar el buzon: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/buscar")
    public Response buscar(@HeaderParam("X-Usuario-Id") String solicitante,
                           @QueryParam("usuarioId") UUID usuarioId,
                           @QueryParam("q") String q) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        if (q == null || q.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parametro q es obligatorio.").build();
        }
        try {
            return Response.ok(correoService.buscar(usuarioId, q.trim())).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{enrutamientoId}/leido")
    public Response marcarLeido(
            @HeaderParam("X-Usuario-Id") String solicitante,
            @PathParam("enrutamientoId") UUID enrutamientoId,
            @QueryParam("usuarioId") UUID usuarioId,
            @QueryParam("leido") boolean leido) {

        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        try {
            boolean actualizado = correoService.cambiarEstadoLectura(enrutamientoId, usuarioId, leido);
            if (actualizado) {
                return Response.ok().entity("Estado de lectura actualizado.").build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Registro de correo no encontrado.").build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{enrutamientoId}/destacado")
    public Response marcarDestacado(
            @HeaderParam("X-Usuario-Id") String solicitante,
            @PathParam("enrutamientoId") UUID enrutamientoId,
            @QueryParam("usuarioId") UUID usuarioId,
            @QueryParam("destacado") boolean destacado) {

        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        try {
            boolean actualizado = correoService.cambiarEstadoDestacado(enrutamientoId, usuarioId, destacado);
            if (actualizado) {
                return Response.ok().entity("Estado destacado actualizado.").build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Registro de correo no encontrado.").build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{enrutamientoId}/mover")
    public Response moverDeCarpeta(
            @HeaderParam("X-Usuario-Id") String solicitante,
            @PathParam("enrutamientoId") UUID enrutamientoId,
            @QueryParam("usuarioId") UUID usuarioId,
            @QueryParam("nuevaCarpetaId") UUID nuevaCarpetaId) {

        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        if (nuevaCarpetaId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El parametro nuevaCarpetaId es obligatorio.").build();
        }
        try {
            boolean movido = correoService.moverMensajeDeCarpeta(enrutamientoId, usuarioId, nuevaCarpetaId);
            if (movido) {
                return Response.ok().entity("Correo movido a la nueva carpeta.").build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Registro de correo no encontrado.").build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{enrutamientoId}")
    public Response eliminarCorreo(@HeaderParam("X-Usuario-Id") String solicitante,
                                   @PathParam("enrutamientoId") UUID enrutamientoId,
                                   @QueryParam("usuarioId") UUID usuarioId) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        try {
            boolean eliminado = correoService.eliminarEnrutamiento(enrutamientoId, usuarioId);
            if (eliminado) {
                return Response.ok().entity("Correo eliminado.").build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("Registro no encontrado.").build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    // Solo el remitente o un destinatario del mensaje pueden leerlo.
    // Si no tiene relación con el mensaje, responde 404 para no revelar que existe.
    @GET
    @Path("/mensajes/{mensajeId}")
    public Response obtenerMensaje(@HeaderParam("X-Usuario-Id") String solicitante,
                                   @PathParam("mensajeId") UUID mensajeId) {
        try {
            UUID usuario = Autorizacion.leer(solicitante);
            if (usuario == null || !enrutamientoService.tieneMensaje(usuario, mensajeId)) {
                return Response.status(Response.Status.NOT_FOUND).entity("Mensaje no encontrado.").build();
            }
            return correoService.obtenerMensaje(mensajeId)
                    .map(m -> Response.ok(m).build())
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                            .entity("Mensaje no encontrado.").build());
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    @POST
    @Path("/mensajes/{mensajeId}/adjuntos")
    @Consumes(MediaType.WILDCARD)
    public Response subirAdjunto(@HeaderParam("X-Usuario-Id") String solicitante,
                                 @PathParam("mensajeId") UUID mensajeId,
                                 @QueryParam("usuarioId") UUID usuarioId,
                                 @HeaderParam("X-Nombre-Archivo") String nombreCodificado,
                                 @HeaderParam(HttpHeaders.CONTENT_TYPE) String mime,
                                 InputStream cuerpo) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        if (nombreCodificado == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El encabezado X-Nombre-Archivo es obligatorio.").build();
        }
        try {
            String nombre = URLDecoder.decode(nombreCodificado, StandardCharsets.UTF_8);
            Adjunto adjunto = adjuntoService.guardar(mensajeId, usuarioId, nombre, mime, cuerpo.readAllBytes());
            return Response.status(Response.Status.CREATED).entity(adjunto).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (SQLException | IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al guardar el adjunto: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/mensajes/{mensajeId}/adjuntos")
    public Response listarAdjuntos(@HeaderParam("X-Usuario-Id") String solicitante,
                                   @PathParam("mensajeId") UUID mensajeId) {
        try {
            UUID usuario = Autorizacion.leer(solicitante);
            if (usuario == null || !enrutamientoService.tieneMensaje(usuario, mensajeId)) {
                return Response.status(Response.Status.NOT_FOUND).entity("Mensaje no encontrado.").build();
            }
            return Response.ok(adjuntoService.listarPorMensaje(mensajeId)).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    // Solo el remitente o un destinatario del mensaje al que pertenece el adjunto pueden descargarlo.
    @GET
    @Path("/adjuntos/{adjuntoId}")
    @Produces(MediaType.WILDCARD)
    public Response descargarAdjunto(@HeaderParam("X-Usuario-Id") String solicitante,
                                     @PathParam("adjuntoId") UUID adjuntoId) {
        try {
            Optional<Adjunto> opcional = adjuntoService.obtener(adjuntoId);
            UUID usuario = Autorizacion.leer(solicitante);
            if (opcional.isEmpty() || usuario == null
                    || !enrutamientoService.tieneMensaje(usuario, opcional.get().getMensajeId())) {
                return Response.status(Response.Status.NOT_FOUND).entity("Adjunto no encontrado.").build();
            }
            Adjunto adjunto = opcional.get();
            byte[] datos = adjuntoService.leerArchivo(adjunto);
            String nombre = URLEncoder.encode(adjunto.getNombreArchivo(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            return Response.ok(datos, adjunto.getTipoMime())
                    .header("Content-Disposition", "attachment; filename*=UTF-8''" + nombre)
                    .build();
        } catch (SQLException | IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al descargar el adjunto: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/cuota")
    public Response obtenerCuota(@HeaderParam("X-Usuario-Id") String solicitante,
                                 @QueryParam("usuarioId") UUID usuarioId) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        try {
            Map<String, Long> datos = Map.of(
                    "usadoBytes", adjuntoService.usoBytes(usuarioId),
                    "cuotaBytes", adjuntoService.cuotaBytes(usuarioId));
            return Response.ok(datos).build();
        } catch (SQLException | IllegalArgumentException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al consultar la cuota: " + e.getMessage()).build();
        }
    }

    // ---- Adjuntos: CRUD sobre los adjuntos enviados por el usuario ----

    @GET
    @Path("/adjuntos")
    public Response listarMisAdjuntos(@HeaderParam("X-Usuario-Id") String solicitante,
                                      @QueryParam("usuarioId") UUID usuarioId) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        try {
            return Response.ok(adjuntoService.listarPorRemitente(usuarioId)).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/adjuntos/{adjuntoId}/datos")
    public Response datosAdjunto(@HeaderParam("X-Usuario-Id") String solicitante,
                                 @PathParam("adjuntoId") UUID adjuntoId,
                                 @QueryParam("usuarioId") UUID usuarioId) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        try {
            return adjuntoService.obtenerDeRemitente(adjuntoId, usuarioId)
                    .map(a -> Response.ok(a).build())
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                            .entity("Adjunto no encontrado.").build());
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/adjuntos/{adjuntoId}")
    public Response eliminarAdjunto(@HeaderParam("X-Usuario-Id") String solicitante,
                                    @PathParam("adjuntoId") UUID adjuntoId,
                                    @QueryParam("usuarioId") UUID usuarioId) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        try {
            if (adjuntoService.eliminarDeRemitente(adjuntoId, usuarioId)) {
                return Response.noContent().build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("Adjunto no encontrado.").build();
        } catch (SQLException | IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al eliminar el adjunto: " + e.getMessage()).build();
        }
    }

    // ---- Enrutamientos: CRUD sobre los registros de buzón del usuario ----

    @GET
    @Path("/enrutamientos")
    public Response listarEnrutamientos(@HeaderParam("X-Usuario-Id") String solicitante,
                                        @QueryParam("usuarioId") UUID usuarioId) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        try {
            return Response.ok(enrutamientoService.listarPorDestinatario(usuarioId)).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/enrutamientos/{id}")
    public Response obtenerEnrutamiento(@HeaderParam("X-Usuario-Id") String solicitante,
                                        @PathParam("id") UUID id,
                                        @QueryParam("usuarioId") UUID usuarioId) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        try {
            return enrutamientoService.obtenerDeUsuario(id, usuarioId)
                    .map(e -> Response.ok(e).build())
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                            .entity("Registro no encontrado.").build());
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    @POST
    @Path("/enrutamientos")
    public Response crearEnrutamiento(@HeaderParam("X-Usuario-Id") String solicitante,
                                      @QueryParam("usuarioId") UUID usuarioId,
                                      EnrutamientoBuzon datos) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        try {
            return Response.status(Response.Status.CREATED)
                    .entity(enrutamientoService.crear(datos, usuarioId)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/enrutamientos/{id}")
    public Response actualizarEnrutamiento(@HeaderParam("X-Usuario-Id") String solicitante,
                                           @PathParam("id") UUID id,
                                           @QueryParam("usuarioId") UUID usuarioId,
                                           EnrutamientoBuzon datos) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        try {
            if (enrutamientoService.actualizar(id, usuarioId, datos)) {
                return Response.ok("Registro actualizado.").build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("Registro no encontrado.").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    private static Response noAutorizado() {
        return Response.status(Response.Status.FORBIDDEN)
                .entity("No autorizado.").build();
    }
}