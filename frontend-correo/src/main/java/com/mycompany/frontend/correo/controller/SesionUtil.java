package com.mycompany.frontend.correo.controller;

import com.mycompany.frontend.correo.model.BackendClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SesionUtil {

    public static BackendClient cliente(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null || sesion.getAttribute("usuarioId") == null) {
            return null;
        }
        return new BackendClient((String) sesion.getAttribute("usuarioId"),
                (String) sesion.getAttribute("carpetaEntradaId"));
    }
}