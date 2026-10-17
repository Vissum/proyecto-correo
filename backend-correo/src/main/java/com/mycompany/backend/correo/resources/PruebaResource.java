package com.mycompany.backend.correo.resources;

import com.mycompany.backend.correo.DatabaseConnection;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Path("prueba-db")
public class PruebaResource {

    @Inject
    private DatabaseConnection databaseConnection;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response probarConexion() {
        String sql = "SELECT version();";
        
        try (Connection conn = databaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                String versionPostgres = rs.getString(1);
                return Response.ok("{\"estado\": \"Conectado correctamente\", \"version\": \"" + versionPostgres.replace("\"", "'") + "\"}")
                               .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .entity("{\"error\": \"No se obtuvo respuesta de PostgreSQL\"}")
                               .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity("{\"error\": \"Error de conexion\", \"detalle\": \"" + e.getMessage() + "\"}")
                           .build();
        }
    }
}