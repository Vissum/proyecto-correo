package com.mycompany.frontend.correo.controller;

import com.mycompany.frontend.correo.model.BackendClient;
import jakarta.json.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/redactar")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 60 * 1024 * 1024)
public class RedactarServlet extends HttpServlet {

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
        if (id != null && mensajeId != null) {
            try {
                request.setAttribute("borrador", cliente.leerCorreo(id, mensajeId));
                JsonObject datos = cliente.leerBorrador(mensajeId);
                request.setAttribute("borradorPara", datos.getString("borradorPara", ""));
                request.setAttribute("borradorCc", datos.getString("borradorCc", ""));
                request.setAttribute("borradorCco", datos.getString("borradorCco", ""));
                request.setAttribute("adjuntos", cliente.listarAdjuntos(mensajeId));
            } catch (Exception e) {
                request.setAttribute("error", "No se pudo abrir el borrador: " + e.getMessage());
            }
        }

        try {
            JsonObject cuota = cliente.obtenerCuota();
            double usadoMb = cuota.getJsonNumber("usadoBytes").longValue() / 1048576.0;
            long cuotaMb = cuota.getJsonNumber("cuotaBytes").longValue() / 1048576L;
            request.setAttribute("cuotaTexto", String.format("%.2f MB de %d MB", usadoMb, cuotaMb));
        } catch (Exception e) {
            request.setAttribute("cuotaTexto", "no disponible");
        }

        request.getRequestDispatcher("/WEB-INF/views/redactar.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BackendClient cliente = SesionUtil.cliente(request);
        if (cliente == null) {
            response.sendRedirect("login");
            return;
        }
        String asunto = request.getParameter("asunto");
        String cuerpo = request.getParameter("cuerpo");
        String borradorMensajeId = vacioANull(request.getParameter("borradorMensajeId"));
        List<String> para = separar(request.getParameter("para"));
        List<String> cc = separar(request.getParameter("cc"));
        List<String> cco = separar(request.getParameter("cco"));
        try {
            validarCuota(cliente, request);

            if (request.getParameter("guardarBorrador") != null) {
                String mensajeId = cliente.guardarBorrador(borradorMensajeId, para, cc, cco, asunto, cuerpo);
                subirArchivos(cliente, request, mensajeId);
                response.sendRedirect("borradores");
                return;
            }

            String mensajeId = cliente.enviar(para, cc, cco, asunto, cuerpo, borradorMensajeId);
            subirArchivos(cliente, request, mensajeId);
            response.sendRedirect("bandeja");
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/redactar.jsp").forward(request, response);
        }
    }

    // Revisa la cuota antes de enviar o guardar, para no enviar un correo sin sus archivos
    private static void validarCuota(BackendClient cliente, HttpServletRequest request) throws Exception {
        long pendiente = 0;
        for (Part parte : request.getParts()) {
            if ("archivos".equals(parte.getName())) {
                pendiente += parte.getSize();
            }
        }
        JsonObject cuota = cliente.obtenerCuota();
        long usado = cuota.getJsonNumber("usadoBytes").longValue();
        long limite = cuota.getJsonNumber("cuotaBytes").longValue();
        if (usado + pendiente > limite) {
            throw new IllegalArgumentException("No tienes espacio suficiente en tu cuota para estos archivos.");
        }
    }

    private static void subirArchivos(BackendClient cliente, HttpServletRequest request, String mensajeId)
            throws Exception {
        for (Part parte : request.getParts()) {
            if ("archivos".equals(parte.getName()) && parte.getSize() > 0) {
                cliente.subirAdjunto(mensajeId, parte.getSubmittedFileName(),
                        parte.getContentType(), parte.getInputStream().readAllBytes());
            }
        }
    }

    private static String vacioANull(String texto) {
        return texto == null || texto.isBlank() ? null : texto;
    }

    private static List<String> separar(String texto) {
        List<String> lista = new ArrayList<>();
        if (texto == null) {
            return lista;
        }
        for (String parte : texto.split("[,;]")) {
            String correo = parte.trim();
            if (!correo.isEmpty()) {
                lista.add(correo);
            }
        }
        return lista;
    }
}