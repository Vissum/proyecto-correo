package com.mycompany.frontend.correo.controller;

import com.mycompany.frontend.correo.model.BackendClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.UUID;

@WebServlet("/descargar")
public class DescargarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BackendClient cliente = SesionUtil.cliente(request);
        if (cliente == null) {
            response.sendRedirect("login");
            return;
        }

        String adjuntoId = request.getParameter("id");
        try {
            UUID.fromString(adjuntoId);
            HttpResponse<byte[]> resp = BackendClient.descargarAdjunto(cliente.getUsuarioId(), adjuntoId);
            String tipo = resp.headers().firstValue("Content-Type").orElse("application/octet-stream");
            String disposicion = resp.headers().firstValue("Content-Disposition").orElse("attachment");
            response.setContentType(tipo);
            response.setHeader("Content-Disposition", disposicion);
            response.setContentLength(resp.body().length);
            response.getOutputStream().write(resp.body());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se pudo descargar el adjunto.");
        }
    }
}