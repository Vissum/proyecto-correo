package com.mycompany.frontend.correo.controller;

import com.mycompany.frontend.correo.model.BackendClient;
import com.mycompany.frontend.correo.model.MovimientoClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/mover")
public class MoverServlet extends HttpServlet {

    // Solo se permite mover a estas carpetas del sistema.
    private static final List<String> DESTINOS_PERMITIDOS = List.of("Entrada", "Archivo", "Spam", "Papelera");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BackendClient cliente = SesionUtil.cliente(request);
        if (cliente == null) {
            response.sendRedirect("login");
            return;
        }

        String id = request.getParameter("id");
        String carpeta = request.getParameter("carpeta");
        try {
            UUID.fromString(id);
            if (carpeta == null || !DESTINOS_PERMITIDOS.contains(carpeta)) {
                throw new IllegalArgumentException("Carpeta no válida.");
            }
            String carpetaId = BackendClient.buscarCarpetaPorNombre(cliente.getUsuarioId(), carpeta);
            MovimientoClient.mover(id, cliente.getUsuarioId(), carpetaId);
        } catch (Exception e) {
            // Si algo falla, regresa a la bandeja sin cambios.
        }
        response.sendRedirect("bandeja");
    }
}