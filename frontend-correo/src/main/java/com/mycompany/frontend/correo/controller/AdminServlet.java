package com.mycompany.frontend.correo.controller;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private static final String BASE_BACKEND = System.getenv().getOrDefault("BACKEND_URL",
            "http://localhost:8080/backend-correo-1.0-SNAPSHOT");
    private static final String RECURSOS = BASE_BACKEND + "/resources";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private static final List<String> TABLAS = List.of("usuarios", "carpetas", "adjuntos", "enrutamientos");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usuarioId = usuarioSesion(request);
        if (usuarioId == null) {
            response.sendRedirect("login");
            return;
        }
        if (!esAdministradorSesion(request)) {
            response.sendRedirect("bandeja");
            return;
        }
        String tabla = tablaValida(request.getParameter("tabla"));
        request.setAttribute("tabla", tabla);
        request.setAttribute("tablas", TABLAS);
        request.setAttribute("msg", decodificar(request.getParameter("msg")));
        request.setAttribute("error", decodificar(request.getParameter("error")));
        try {
            String id = vacioANull(request.getParameter("id"));
            if (id != null) {
                request.setAttribute("detalle", leer(obtener(tabla, requerirId(id), usuarioId)));
            } else {
                request.setAttribute("filas", leer(listar(tabla, usuarioId)));
            }
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
        }
        request.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usuarioId = usuarioSesion(request);
        if (usuarioId == null) {
            response.sendRedirect("login");
            return;
        }
        if (!esAdministradorSesion(request)) {
            response.sendRedirect("bandeja");
            return;
        }
        String tabla = tablaValida(request.getParameter("tabla"));
        try {
            String accion = request.getParameter("accion") == null ? "" : request.getParameter("accion");
            String id = vacioANull(request.getParameter("id"));
            switch (accion) {
                case "crear" -> crear(tabla, usuarioId, request);
                case "actualizar" -> actualizar(tabla, requerirId(id), usuarioId, request);
                case "eliminar" -> eliminar(tabla, requerirId(id), usuarioId);
                default -> throw new IllegalArgumentException("Acción no válida.");
            }
            redirigir(response, tabla, "msg", "Operación realizada correctamente.");
        } catch (Exception e) {
            redirigir(response, tabla, "error", e.getMessage());
        }
    }

    // ---------- Operaciones por tabla ----------

    private static void crear(String tabla, String usuarioId, HttpServletRequest r) throws Exception {
        switch (tabla) {
            case "usuarios" -> peticion(usuarioId, "POST", RECURSOS + "/usuarios", Json.createObjectBuilder()
                    .add("correo", requerido(r, "correo"))
                    .add("contrasenaHash", requerido(r, "contrasena"))
                    .add("nombreCompleto", requerido(r, "nombreCompleto"))
                    .add("estado", opcional(r, "estado", "activo"))
                    .add("cuotaAlmacenamientoMb", entero(r, "cuotaAlmacenamientoMb", 1024))
                    .build());
            case "carpetas" -> peticion(usuarioId, "POST", RECURSOS + "/carpetas?usuarioId=" + usuarioId,
                    Json.createObjectBuilder().add("nombre", requerido(r, "nombre")).build());
            case "enrutamientos" -> peticion(usuarioId, "POST", RECURSOS + "/correos/enrutamientos?usuarioId=" + usuarioId,
                    Json.createObjectBuilder()
                            .add("mensajeId", requerido(r, "mensajeId"))
                            .add("carpetaId", requerido(r, "carpetaId"))
                            .add("tipoEnvio", opcional(r, "tipoEnvio", "para"))
                            .add("leido", r.getParameter("leido") != null)
                            .add("destacado", r.getParameter("destacado") != null)
                            .build());
            default -> throw new IllegalArgumentException(
                    "Los adjuntos se crean desde Redactar, al adjuntar archivos a un correo o borrador.");
        }
    }

    private static void actualizar(String tabla, String id, String usuarioId, HttpServletRequest r) throws Exception {
        switch (tabla) {
            case "usuarios" -> peticion(usuarioId, "PUT", RECURSOS + "/usuarios/" + id, Json.createObjectBuilder()
                    .add("nombreCompleto", requerido(r, "nombreCompleto"))
                    .add("estado", requerido(r, "estado"))
                    .add("cuotaAlmacenamientoMb", entero(r, "cuotaAlmacenamientoMb", 1024))
                    .build());
            case "carpetas" -> peticion(usuarioId, "PUT", RECURSOS + "/carpetas/" + id + "?usuarioId=" + usuarioId,
                    Json.createObjectBuilder().add("nombre", requerido(r, "nombre")).build());
            case "enrutamientos" -> peticion(usuarioId, "PUT",
                    RECURSOS + "/correos/enrutamientos/" + id + "?usuarioId=" + usuarioId,
                    Json.createObjectBuilder()
                            .add("carpetaId", requerido(r, "carpetaId"))
                            .add("leido", r.getParameter("leido") != null)
                            .add("destacado", r.getParameter("destacado") != null)
                            .build());
            default -> throw new IllegalArgumentException("Los adjuntos no se editan.");
        }
    }

    private static void eliminar(String tabla, String id, String usuarioId) throws Exception {
        String url = switch (tabla) {
            case "usuarios" -> RECURSOS + "/usuarios/" + id;
            case "carpetas" -> RECURSOS + "/carpetas/" + id + "?usuarioId=" + usuarioId;
            case "adjuntos" -> RECURSOS + "/correos/adjuntos/" + id + "?usuarioId=" + usuarioId;
            default -> RECURSOS + "/correos/" + id + "?usuarioId=" + usuarioId;
        };
        peticion(usuarioId, "DELETE", url, null);
    }

    private static String obtener(String tabla, String id, String usuarioId) throws Exception {
        String url = switch (tabla) {
            case "usuarios" -> RECURSOS + "/usuarios/" + id;
            case "carpetas" -> RECURSOS + "/carpetas/" + id + "?usuarioId=" + usuarioId;
            case "adjuntos" -> RECURSOS + "/correos/adjuntos/" + id + "/datos?usuarioId=" + usuarioId;
            default -> RECURSOS + "/correos/enrutamientos/" + id + "?usuarioId=" + usuarioId;
        };
        return peticion(usuarioId, "GET", url, null);
    }

    private static String listar(String tabla, String usuarioId) throws Exception {
        String url = switch (tabla) {
            case "usuarios" -> RECURSOS + "/usuarios";
            case "carpetas" -> RECURSOS + "/carpetas/usuario/" + usuarioId;
            case "adjuntos" -> RECURSOS + "/correos/adjuntos?usuarioId=" + usuarioId;
            default -> RECURSOS + "/correos/enrutamientos?usuarioId=" + usuarioId;
        };
        return peticion(usuarioId, "GET", url, null);
    }

    // ---------- HTTP y utilidades ----------

    // Todas las peticiones envían la identidad de quien hace la acción en X-Usuario-Id.
    private static String peticion(String usuarioId, String metodo, String url, JsonObject cuerpo)
            throws IOException, InterruptedException {
        HttpRequest.BodyPublisher publicador = cuerpo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(cuerpo.toString(), StandardCharsets.UTF_8);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .header("X-Usuario-Id", usuarioId)
                .method(metodo, publicador).build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() / 100 != 2) {
            throw new IOException(resp.body());
        }
        return resp.body();
    }

    private static JsonValue leer(String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank()) {
            return JsonValue.NULL;
        }
        try (JsonReader lector = Json.createReader(new StringReader(cuerpo))) {
            return lector.readValue();
        }
    }

    private static String usuarioSesion(HttpServletRequest r) {
        HttpSession sesion = r.getSession(false);
        return sesion == null ? null : (String) sesion.getAttribute("usuarioId");
    }

    private static boolean esAdministradorSesion(HttpServletRequest r) {
        HttpSession sesion = r.getSession(false);
        return sesion != null && Boolean.TRUE.equals(sesion.getAttribute("esAdministrador"));
    }

    private static String tablaValida(String tabla) {
        return tabla != null && TABLAS.contains(tabla) ? tabla : "usuarios";
    }

    private static String vacioANull(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private static String requerirId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Falta el ID.");
        }
        try {
            return UUID.fromString(id).toString();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El ID no tiene un formato válido.");
        }
    }

    private static String requerido(HttpServletRequest r, String nombre) {
        String valor = vacioANull(r.getParameter(nombre));
        if (valor == null) {
            throw new IllegalArgumentException("Falta el campo " + nombre + ".");
        }
        return valor;
    }

    private static String opcional(HttpServletRequest r, String nombre, String porDefecto) {
        String valor = vacioANull(r.getParameter(nombre));
        return valor == null ? porDefecto : valor;
    }

    private static int entero(HttpServletRequest r, String nombre, int porDefecto) {
        String valor = vacioANull(r.getParameter(nombre));
        if (valor == null) {
            return porDefecto;
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + nombre + " debe ser un número.");
        }
    }

    private static void redirigir(HttpServletResponse response, String tabla, String clave, String texto)
            throws IOException {
        String valor = URLEncoder.encode(texto == null ? "Error desconocido." : texto, StandardCharsets.UTF_8);
        response.sendRedirect("admin?tabla=" + tabla + "&" + clave + "=" + valor);
    }

    private static String decodificar(String texto) {
        if (texto == null) {
            return null;
        }
        return new String(texto.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }
}
