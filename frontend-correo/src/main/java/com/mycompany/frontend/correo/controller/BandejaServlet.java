package com.mycompany.frontend.correo.controller;

import com.mycompany.frontend.correo.model.BackendClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/bandeja")
public class BandejaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BackendClient cliente = SesionUtil.cliente(request);
        if (cliente == null) {
            response.sendRedirect("login");
            return;
        }
        try {
            request.setAttribute("correos", cliente.listarBandeja());
        } catch (Exception e) {
            request.setAttribute("error", "No se pudo cargar la bandeja: " + e.getMessage());
        }
        request.getRequestDispatcher("/WEB-INF/views/bandeja.jsp").forward(request, response);
    }
}