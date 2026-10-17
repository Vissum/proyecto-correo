package com.mycompany.frontend.correo.controller;

import com.mycompany.frontend.correo.model.BackendClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/enviados")
public class EnviadosServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BackendClient cliente = SesionUtil.cliente(request);
        if (cliente == null) {
            response.sendRedirect("login");
            return;
        }
        try {
            String usuarioId = (String) request.getSession(false).getAttribute("usuarioId");
            String carpetaId = BackendClient.buscarCarpetaPorNombre(usuarioId, "Enviados");
            request.setAttribute("correos", cliente.listarCarpeta(carpetaId));
        } catch (Exception e) {
            request.setAttribute("error", "No se pudieron cargar los enviados: " + e.getMessage());
        }
        request.setAttribute("titulo", "Enviados");
        request.setAttribute("destino", "leer");
        request.getRequestDispatcher("/WEB-INF/views/bandeja.jsp").forward(request, response);
    }
}