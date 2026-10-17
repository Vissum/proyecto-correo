package com.mycompany.frontend.correo.controller;

import com.mycompany.frontend.correo.model.RecuperacionClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/restablecer")
public class RestablecerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getParameter("token");
        request.setAttribute("token", token == null ? "" : token);
        request.getRequestDispatcher("/WEB-INF/views/restablecer.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String token = request.getParameter("token");
        String contrasena = request.getParameter("contrasena");
        String confirmar = request.getParameter("confirmar");
        request.setAttribute("token", token == null ? "" : token);

        String error = null;
        if (contrasena == null || contrasena.length() < 6) {
            error = "La contraseña debe tener al menos 6 caracteres.";
        } else if (!contrasena.equals(confirmar)) {
            error = "Las contraseñas no coinciden.";
        }

        if (error == null) {
            try {
                RecuperacionClient.restablecer(token, contrasena);
                response.sendRedirect("login?restablecida=1");
                return;
            } catch (Exception e) {
                error = e.getMessage() == null || e.getMessage().isBlank()
                        ? "No se pudo cambiar la contraseña."
                        : e.getMessage();
            }
        }
        request.setAttribute("error", error);
        request.getRequestDispatcher("/WEB-INF/views/restablecer.jsp").forward(request, response);
    }
}