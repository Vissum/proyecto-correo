package com.mycompany.frontend.correo.controller;

import com.mycompany.frontend.correo.model.BackendClient;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            JsonObject usuario = BackendClient.login(
                    request.getParameter("correo"), request.getParameter("contrasena"));
            String usuarioId = usuario.getString("id");
            String carpetaEntrada = BackendClient.buscarCarpetaEntrada(usuarioId);

            // Se invalida la sesión anterior para que no queden datos de otro usuario.
            HttpSession anterior = request.getSession(false);
            if (anterior != null) {
                anterior.invalidate();
            }
            HttpSession sesion = request.getSession(true);
            sesion.setAttribute("usuarioId", usuarioId);
            sesion.setAttribute("carpetaEntradaId", carpetaEntrada);
            sesion.setAttribute("correo", usuario.getString("correo", ""));
            sesion.setAttribute("nombre", usuario.getString("nombreCompleto", usuario.getString("correo")));
            sesion.setAttribute("esAdministrador", usuario.getBoolean("esAdministrador", false));
            response.sendRedirect("bandeja");
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }
}