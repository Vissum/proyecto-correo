package com.mycompany.frontend.correo.controller;

import com.mycompany.frontend.correo.model.BackendClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/buscar")
public class BuscarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BackendClient cliente = SesionUtil.cliente(request);
        if (cliente == null) {
            response.sendRedirect("login");
            return;
        }
        String q = request.getParameter("q");
        if (q == null || q.isBlank()) {
            response.sendRedirect("bandeja");
            return;
        }
        try {
            request.setAttribute("correos", cliente.buscar(q.trim()));
        } catch (Exception e) {
            request.setAttribute("error", "No se pudo realizar la búsqueda: " + e.getMessage());
        }
        request.setAttribute("titulo", "Resultados para \"" + q.trim() + "\"");
        request.setAttribute("destino", "leer");
        request.getRequestDispatcher("/WEB-INF/views/bandeja.jsp").forward(request, response);
    }
}