package com.mycompany.frontend.correo.model;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class BackendClient {

    private static final String BASE = System.getenv().getOrDefault("BACKEND_URL",
            "http://localhost:8080/backend-correo-1.0-SNAPSHOT");
    private static final String RECURSOS = BASE + "/resources";
    private static final String CORREOS = RECURSOS + "/correos";
    private static final String ENCABEZADO_USUARIO = "X-Usuario-Id";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    private final String usuarioId;
    private final String carpetaEntradaId;

    public BackendClient(String usuarioId, String carpetaEntradaId) {
        this.usuarioId = usuarioId;
        this.carpetaEntradaId = carpetaEntradaId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public static JsonObject login(String correo, String contrasena) throws IOException, InterruptedException {
        String json = Json.createObjectBuilder()
                .add("correo", correo)
                .add("contrasena", contrasena == null ? "" : contrasena)
                .build().toString();
        HttpRequest req = HttpRequest.newBuilder(URI.create(RECURSOS + "/usuarios/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException(resp.body());
        }
        return leerObjeto(resp.body());
    }

    public static void registrar(String correo, String contrasena, String nombre)
            throws IOException, InterruptedException {
        String json = Json.createObjectBuilder()
                .add("correo", correo)
                .add("contrasenaHash", contrasena)
                .add("nombreCompleto", nombre)
                .add("estado", "activo")
                .add("cuotaAlmacenamientoMb", 1024)
                .build().toString();
        HttpRequest req = HttpRequest.newBuilder(URI.create(RECURSOS + "/usuarios"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 201) {
            throw new IOException(resp.body());
        }
    }

    public static String buscarCarpetaEntrada(String usuarioId) throws IOException, InterruptedException {
        return buscarCarpetaPorNombre(usuarioId, "Entrada");
    }

    public static String buscarCarpetaPorNombre(String usuarioId, String nombre)
            throws IOException, InterruptedException {
        JsonArray carpetas = leerArray(get(RECURSOS + "/carpetas/usuario/" + usuarioId, usuarioId));
        for (JsonValue v : carpetas) {
            JsonObject c = v.asJsonObject();
            if (nombre.equals(c.getString("nombre", ""))) {
                return c.getString("id");
            }
        }
        throw new IOException("No se encontró la carpeta " + nombre + " del usuario.");
    }

    public List<CorreoResumen> listarBandeja() throws IOException, InterruptedException {
        return listarCarpeta(carpetaEntradaId);
    }

    public List<CorreoResumen> listarCarpeta(String carpetaId) throws IOException, InterruptedException {
        JsonArray buzon = leerArray(get(CORREOS + "/buzon?usuarioId=" + usuarioId
                + "&carpetaId=" + carpetaId, usuarioId));
        return convertir(buzon);
    }

    public List<CorreoResumen> buscar(String texto) throws IOException, InterruptedException {
        JsonArray resultados = leerArray(get(CORREOS + "/buscar?usuarioId=" + usuarioId
                + "&q=" + URLEncoder.encode(texto, StandardCharsets.UTF_8), usuarioId));
        return convertir(resultados);
    }

    private List<CorreoResumen> convertir(JsonArray filas) throws IOException, InterruptedException {
        List<CorreoResumen> lista = new ArrayList<>();
        for (JsonValue v : filas) {
            JsonObject e = v.asJsonObject();
            String mensajeId = e.getString("mensajeId");
            JsonObject m = leerObjeto(get(CORREOS + "/mensajes/" + mensajeId, usuarioId));
            lista.add(new CorreoResumen(
                    e.getString("id"), mensajeId,
                    m.getString("asunto", ""), m.getString("cuerpoPlano", ""),
                    formatear(e.getString("fechaRecepcion", "")),
                    e.getBoolean("leido"),
                    e.getBoolean("destacado", false)));
        }
        return lista;
    }

    public CorreoResumen leerCorreo(String enrutamientoId, String mensajeId)
            throws IOException, InterruptedException {
        marcarLeido(enrutamientoId);
        JsonObject m = leerObjeto(get(CORREOS + "/mensajes/" + mensajeId, usuarioId));
        return new CorreoResumen(enrutamientoId, mensajeId,
                m.getString("asunto", ""), m.getString("cuerpoPlano", ""),
                formatear(m.getString("fechaEnvio", "")), true);
    }

    public String enviar(List<String> para, List<String> cc, List<String> cco,
                         String asunto, String cuerpo, String borradorMensajeId)
            throws IOException, InterruptedException {
        JsonObjectBuilder b = Json.createObjectBuilder()
                .add("remitenteId", usuarioId)
                .add("asunto", asunto == null ? "" : asunto)
                .add("cuerpoPlano", cuerpo == null ? "" : cuerpo)
                .add("paisOrigen", "Guatemala")
                .add("para", arreglo(para))
                .add("cc", arreglo(cc))
                .add("cco", arreglo(cco));
        if (borradorMensajeId != null) {
            b.add("borradorMensajeId", borradorMensajeId);
        }
        HttpRequest req = HttpRequest.newBuilder(URI.create(CORREOS + "/enviar-multiple"))
                .header("Content-Type", "application/json")
                .header(ENCABEZADO_USUARIO, usuarioId)
                .POST(HttpRequest.BodyPublishers.ofString(b.build().toString())).build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 201) {
            throw new IOException(resp.body());
        }
        return leerObjeto(resp.body()).getString("id");
    }

    public String guardarBorrador(String mensajeId, List<String> para, List<String> cc, List<String> cco,
                                  String asunto, String cuerpo) throws IOException, InterruptedException {
        JsonObjectBuilder b = Json.createObjectBuilder()
                .add("remitenteId", usuarioId)
                .add("asunto", asunto == null ? "" : asunto)
                .add("cuerpoPlano", cuerpo == null ? "" : cuerpo)
                .add("paisOrigen", "Guatemala")
                .add("para", arreglo(para))
                .add("cc", arreglo(cc))
                .add("cco", arreglo(cco));
        if (mensajeId != null) {
            b.add("mensajeId", mensajeId);
        }
        HttpRequest req = HttpRequest.newBuilder(URI.create(CORREOS + "/borradores"))
                .header("Content-Type", "application/json")
                .header(ENCABEZADO_USUARIO, usuarioId)
                .POST(HttpRequest.BodyPublishers.ofString(b.build().toString())).build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 201) {
            throw new IOException(resp.body());
        }
        return leerObjeto(resp.body()).getString("id");
    }

    public void eliminarCorreo(String enrutamientoId) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(CORREOS + "/" + enrutamientoId
                        + "?usuarioId=" + usuarioId))
                .header(ENCABEZADO_USUARIO, usuarioId)
                .DELETE().build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException(resp.body());
        }
    }

    public void marcarDestacado(String enrutamientoId, boolean destacado)
            throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(CORREOS + "/" + enrutamientoId
                        + "/destacado?usuarioId=" + usuarioId + "&destacado=" + destacado))
                .header(ENCABEZADO_USUARIO, usuarioId)
                .PUT(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException(resp.body());
        }
    }

    public void subirAdjunto(String mensajeId, String nombre, String mime, byte[] datos)
            throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(CORREOS + "/mensajes/" + mensajeId
                        + "/adjuntos?usuarioId=" + usuarioId))
                .header("Content-Type", mime == null || mime.isBlank() ? "application/octet-stream" : mime)
                .header("X-Nombre-Archivo", URLEncoder.encode(nombre, StandardCharsets.UTF_8))
                .header(ENCABEZADO_USUARIO, usuarioId)
                .POST(HttpRequest.BodyPublishers.ofByteArray(datos)).build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 201) {
            throw new IOException(resp.body());
        }
    }

    public List<AdjuntoResumen> listarAdjuntos(String mensajeId) throws IOException, InterruptedException {
        JsonArray arreglo = leerArray(get(CORREOS + "/mensajes/" + mensajeId + "/adjuntos", usuarioId));
        List<AdjuntoResumen> lista = new ArrayList<>();
        for (JsonValue v : arreglo) {
            JsonObject a = v.asJsonObject();
            lista.add(new AdjuntoResumen(
                    a.getString("id"),
                    a.getString("nombreArchivo", ""),
                    a.getJsonNumber("tamanoBytes").longValue()));
        }
        return lista;
    }

    // Requiere el usuario de la sesión: el backend solo entrega adjuntos de mensajes del usuario.
    public static HttpResponse<byte[]> descargarAdjunto(String usuarioId, String adjuntoId)
            throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(CORREOS + "/adjuntos/" + adjuntoId))
                .header(ENCABEZADO_USUARIO, usuarioId)
                .GET().build();
        HttpResponse<byte[]> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() != 200) {
            throw new IOException("No se pudo descargar el adjunto.");
        }
        return resp;
    }

    private void marcarLeido(String enrutamientoId) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(CORREOS + "/" + enrutamientoId
                        + "/leido?usuarioId=" + usuarioId + "&leido=true"))
                .header(ENCABEZADO_USUARIO, usuarioId)
                .PUT(HttpRequest.BodyPublishers.noBody()).build();
        HTTP.send(req, HttpResponse.BodyHandlers.discarding());
    }

    private static JsonArray arreglo(List<String> valores) {
        JsonArrayBuilder b = Json.createArrayBuilder();
        for (String v : valores) {
            b.add(v);
        }
        return b.build();
    }

    private static String get(String url, String usuarioId) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header(ENCABEZADO_USUARIO, usuarioId)
                .GET().build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("HTTP " + resp.statusCode() + ": " + resp.body());
        }
        return resp.body();
    }

    private static JsonObject leerObjeto(String json) {
        try (JsonReader r = Json.createReader(new StringReader(json))) {
            return r.readObject();
        }
    }

    private static JsonArray leerArray(String json) {
        try (JsonReader r = Json.createReader(new StringReader(json))) {
            return r.readArray();
        }
    }

    private static String formatear(String fecha) {
        return fecha.length() >= 16 ? fecha.substring(0, 16).replace('T', ' ') : fecha;
    }

    public JsonObject leerBorrador(String mensajeId) throws IOException, InterruptedException {
        return leerObjeto(get(CORREOS + "/mensajes/" + mensajeId, usuarioId));
    }

    public JsonObject obtenerCuota() throws IOException, InterruptedException {
        return leerObjeto(get(CORREOS + "/cuota?usuarioId=" + usuarioId, usuarioId));
    }
}