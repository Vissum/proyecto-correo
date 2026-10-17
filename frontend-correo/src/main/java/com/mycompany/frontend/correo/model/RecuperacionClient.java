package com.mycompany.frontend.correo.model;

import jakarta.json.Json;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class RecuperacionClient {

    private static final String BASE = System.getenv().getOrDefault("BACKEND_URL",
            "http://localhost:8080/backend-correo-1.0-SNAPSHOT") + "/resources/usuarios";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    public static void solicitar(String correo) throws IOException, InterruptedException {
        String json = Json.createObjectBuilder()
                .add("correo", correo == null ? "" : correo)
                .build().toString();
        enviar(BASE + "/recuperar", json);
    }

    public static void restablecer(String token, String contrasena) throws IOException, InterruptedException {
        String json = Json.createObjectBuilder()
                .add("token", token == null ? "" : token)
                .add("contrasena", contrasena == null ? "" : contrasena)
                .build().toString();
        enviar(BASE + "/restablecer", json);
    }

    private static void enviar(String url, String json) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() != 200) {
            throw new IOException(resp.body());
        }
    }
}
