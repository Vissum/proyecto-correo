package com.mycompany.frontend.correo.model;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class MovimientoClient {

    private static final String BASE_BACKEND = System.getenv().getOrDefault("BACKEND_URL",
            "http://localhost:8080/backend-correo-1.0-SNAPSHOT");
    private static final String CORREOS = BASE_BACKEND + "/resources/correos";
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    public static void mover(String enrutamientoId, String usuarioId, String carpetaId)
            throws IOException, InterruptedException {
        URI uri = URI.create(CORREOS + "/" + enrutamientoId
                + "/mover?usuarioId=" + usuarioId + "&nuevaCarpetaId=" + carpetaId);
        HttpRequest req = HttpRequest.newBuilder(uri)
                .header("X-Usuario-Id", usuarioId)
                .PUT(HttpRequest.BodyPublishers.noBody()).build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException(resp.body());
        }
    }
}
