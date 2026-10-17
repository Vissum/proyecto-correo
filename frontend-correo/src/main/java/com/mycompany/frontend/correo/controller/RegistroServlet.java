package com.mycompany.frontend.correo.controller;

import com.mycompany.frontend.correo.model.BackendClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/registro.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nombre = request.getParameter("nombre");
        String correo = request.getParameter("correo");
        String contrasena = request.getParameter("contrasena");
        String confirmar = request.getParameter("confirmar");

        String error = null;
        if (nombre == null || nombre.isBlank() || correo == null || correo.isBlank()) {
            error = "Completa todos los campos.";
        } else if (contrasena == null || contrasena.length() < 6) {
            error = "La contraseña debe tener al menos 6 caracteres.";
        } else if (!contrasena.equals(confirmar)) {
            error = "Las contraseñas no coinciden.";
        }

        if (error == null) {
            try {
                BackendClient.registrar(correo.trim(), contrasena, nombre.trim());
                response.sendRedirect("login?registrado=1");
                return;
            } catch (Exception e) {
                error = "No se pudo crear la cuenta: " + e.getMessage();
            }
        }
        request.setAttribute("error", error);
        request.getRequestDispatcher("/WEB-INF/views/registro.jsp").forward(request, response);
    }
}