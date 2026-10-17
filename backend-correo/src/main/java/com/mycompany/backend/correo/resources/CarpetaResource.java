package com.mycompany.backend.correo.resources;

import com.mycompany.backend.correo.model.Carpeta;
import com.mycompany.backend.correo.security.Autorizacion;
import com.mycompany.backend.correo.service.CarpetaService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Path("/carpetas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CarpetaResource {

    @Inject
    private CarpetaService carpetaService;

    @POST
    public Response crearCarpeta(@HeaderParam("X-Usuario-Id") String solicitante,
                                 @QueryParam("usuarioId") UUID usuarioId,
                                 Carpeta carpeta) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        if (carpeta == null || carpeta.getNombre() == null || carpeta.getNombre().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El nombre de la carpeta es obligatorio.").build();
        }
        try {
            Carpeta nuevaCarpeta = carpetaService.crearCarpeta(usuarioId, carpeta.getNombre());
            return Response.status(Response.Status.CREATED).entity(nuevaCarpeta).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/usuario/{usuarioId}")
    public Response listarPorUsuario(@HeaderParam("X-Usuario-Id") String solicitante,
                                     @PathParam("usuarioId") UUID usuarioId) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        try {
            List<Carpeta> carpetas = carpetaService.listarPorUsuario(usuarioId);
            return Response.ok(carpetas).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al consultar carpetas: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}")
    public Response obtenerCarpeta(@HeaderParam("X-Usuario-Id") String solicitante,
                                   @PathParam("id") UUID id,
                                   @QueryParam("usuarioId") UUID usuarioId) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        try {
            return carpetaService.obtenerPorId(id, usuarioId)
                    .map(c -> Response.ok(c).build())
                    .orElseGet(() -> Response.status(Response.Status.NOT_FOUND)
                            .entity("Carpeta no encontrada.").build());
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response renombrarCarpeta(@HeaderParam("X-Usuario-Id") String solicitante,
                                     @PathParam("id") UUID id,
                                     @QueryParam("usuarioId") UUID usuarioId,
                                     Carpeta carpeta) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        if (carpeta == null || carpeta.getNombre() == null || carpeta.getNombre().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El nuevo nombre es obligatorio.").build();
        }
        try {
            boolean actualizado = carpetaService.renombrarCarpeta(id, usuarioId, carpeta.getNombre());
            if (actualizado) {
                return Response.ok().entity("Carpeta renombrada exitosamente.").build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No se pudo renombrar la carpeta (no existe o es de sistema).").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error en la base de datos: " + e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response eliminarCarpeta(@HeaderParam("X-Usuario-Id") String solicitante,
                                    @PathParam("id") UUID id,
                                    @QueryParam("usuarioId") UUID usuarioId) {
        if (!Autorizacion.esMismoUsuario(solicitante, usuarioId)) {
            return noAutorizado();
        }
        try {
            boolean eliminado = carpetaService.eliminarCarpeta(id, usuarioId);
            if (eliminado) {
                return Response.noContent().build();
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No se pudo eliminar la carpeta (no existe o es de sistema).").build();
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