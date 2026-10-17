package com.mycompany.frontend.correo.controller;

import com.mycompany.frontend.correo.model.BackendClient;
import com.mycompany.frontend.correo.model.CorreoResumen;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet({"/destacados", "/archivo", "/spam", "/papelera"})
public class VistaCarpetaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BackendClient cliente = SesionUtil.cliente(request);
        if (cliente == null) {
            response.sendRedirect("login");
            return;
        }

        String ruta = request.getServletPath();
        String titulo;
        List<CorreoResumen> correos = new ArrayList<>();
        try {
            if ("/destacados".equals(ruta)) {
                titulo = "Destacados";
                // Destacados se toma de Entrada, Archivo y Enviados; Spam y Papelera quedan fuera.
                for (String nombre : List.of("Entrada", "Archivo", "Enviados")) {
                    String carpetaId = BackendClient.buscarCarpetaPorNombre(cliente.getUsuarioId(), nombre);
                    for (CorreoResumen c : cliente.listarCarpeta(carpetaId)) {
                        if (c.destacado()) {
                            correos.add(c);
                        }
                    }
                }
            } else {
                String nombre;
                if ("/archivo".equals(ruta)) {
                    nombre = "Archivo";
                } else if ("/spam".equals(ruta)) {
                    nombre = "Spam";
                } else {
                    nombre = "Papelera";
                }
                titulo = nombre;
                String carpetaId = BackendClient.buscarCarpetaPorNombre(cliente.getUsuarioId(), nombre);
                correos = cliente.listarCarpeta(carpetaId);
            }
        } catch (Exception e) {
            titulo = "Correo";
            request.setAttribute("error", "No se pudo cargar la carpeta: " + e.getMessage());
        }

        request.setAttribute("titulo", titulo);
        request.setAttribute("destino", "leer");
        request.setAttribute("correos", correos);
        request.getRequestDispatcher("/WEB-INF/views/bandeja.jsp").forward(request, response);
    }
}