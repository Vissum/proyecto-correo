package com.mycompany.frontend.correo.controller;

import com.mycompany.frontend.correo.model.BackendClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/leer")
public class LeerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BackendClient cliente = SesionUtil.cliente(request);
        if (cliente == null) {
            response.sendRedirect("login");
            return;
        }
        String id = request.getParameter("id");
        String mensajeId = request.getParameter("mensajeId");
        try {
            request.setAttribute("correo", cliente.leerCorreo(id, mensajeId));
            request.setAttribute("adjuntos", cliente.listarAdjuntos(mensajeId));
        } catch (Exception e) {
            request.setAttribute("error", "No se pudo abrir el correo: " + e.getMessage());
        }
        request.getRequestDispatcher("/WEB-INF/views/leer.jsp").forward(request, response);
    }
}