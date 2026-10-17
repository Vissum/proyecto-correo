package com.mycompany.frontend.correo.controller;

import com.mycompany.frontend.correo.model.RecuperacionClient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/recuperar")
public class RecuperarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/recuperar.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String correo = request.getParameter("correo");
        try {
            RecuperacionClient.solicitar(correo == null ? "" : correo.trim());
            request.setAttribute("mensaje",
                    "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña. "
                            + "Revisa también tu carpeta de spam.");
        } catch (Exception e) {
            request.setAttribute("error", "No se pudo procesar tu solicitud. Intenta de nuevo más tarde.");
        }
        request.getRequestDispatcher("/WEB-INF/views/recuperar.jsp").forward(request, response);
    }
}