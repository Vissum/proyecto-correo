package com.mycompany.backend.correo.service;

import com.mycompany.backend.correo.dao.AdjuntoDAO;
import com.mycompany.backend.correo.dao.MensajeDAO;
import com.mycompany.backend.correo.dao.UsuarioDAO;
import com.mycompany.backend.correo.model.Adjunto;
import com.mycompany.backend.correo.model.Mensaje;
import com.mycompany.backend.correo.model.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AdjuntoService {

    // Carpeta donde se guardan los archivos adjuntos. Se puede cambiar con la variable ADJUNTOS_DIR.
    private static final Path CARPETA = Paths.get(
            System.getenv().getOrDefault("ADJUNTOS_DIR", "C:\\correo_adjuntos"));
    private static final long TAMANO_MAXIMO = 10L * 1024 * 1024;
    private static final int CUOTA_POR_DEFECTO_MB = 1024;

    @Inject
    private AdjuntoDAO adjuntoDAO;

    @Inject
    private MensajeDAO mensajeDAO;

    @Inject
    private UsuarioDAO usuarioDAO;

    public Adjunto guardar(UUID mensajeId, UUID usuarioId, String nombre, String mime, byte[] datos)
            throws SQLException, IOException {
        Mensaje mensaje = mensajeDAO.obtenerPorId(mensajeId)
                .orElseThrow(() -> new IllegalArgumentException("El mensaje no existe."));
        if (!usuarioId.equals(mensaje.getRemitenteId())) {
            throw new IllegalArgumentException("Solo el remitente puede adjuntar archivos.");
        }
        if (datos.length == 0) {
            throw new IllegalArgumentException("El archivo esta vacio.");
        }
        if (datos.length > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("El archivo supera 10 MB.");
        }
        if (usoBytes(usuarioId) + datos.length > cuotaBytes(usuarioId)) {
            throw new IllegalArgumentException("No tienes espacio suficiente en tu cuota de almacenamiento.");
        }

        Files.createDirectories(CARPETA);
        Path destino = CARPETA.resolve(UUID.randomUUID().toString());
        Files.write(destino, datos);

        Adjunto adjunto = new Adjunto();
        adjunto.setMensajeId(mensajeId);
        adjunto.setNombreArchivo(nombre);
        adjunto.setTipoMime(mime != null && !mime.isBlank() ? mime : "application/octet-stream");
        adjunto.setTamanoBytes((long) datos.length);
        adjunto.setRutaServidor(destino.toString());
        return adjuntoDAO.crear(adjunto);
    }

    public List<Adjunto> listarPorMensaje(UUID mensajeId) throws SQLException {
        return adjuntoDAO.listarPorMensaje(mensajeId);
    }

    public Optional<Adjunto> obtener(UUID id) throws SQLException {
        return adjuntoDAO.obtenerPorId(id);
    }

    public byte[] leerArchivo(Adjunto adjunto) throws IOException {
        return Files.readAllBytes(rutaFisica(adjunto));
    }

    public long usoBytes(UUID usuarioId) throws SQLException {
        return adjuntoDAO.usoAlmacenamientoBytes(usuarioId);
    }

    public long cuotaBytes(UUID usuarioId) throws SQLException {
        Usuario usuario = usuarioDAO.obtenerPorId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        Integer mb = usuario.getCuotaAlmacenamientoMb();
        return (long) (mb != null ? mb : CUOTA_POR_DEFECTO_MB) * 1024 * 1024;
    }

    public List<Adjunto> listarPorRemitente(UUID remitenteId) throws SQLException {
        return adjuntoDAO.listarPorRemitente(remitenteId);
    }

    public Optional<Adjunto> obtenerDeRemitente(UUID adjuntoId, UUID remitenteId) throws SQLException {
        Optional<Adjunto> adjunto = adjuntoDAO.obtenerPorId(adjuntoId);
        if (adjunto.isEmpty()) {
            return adjunto;
        }
        return mensajeDAO.obtenerPorId(adjunto.get().getMensajeId())
                .filter(m -> remitenteId.equals(m.getRemitenteId()))
                .map(m -> adjunto.get());
    }

    public boolean eliminarDeRemitente(UUID adjuntoId, UUID remitenteId) throws SQLException, IOException {
        Optional<Adjunto> adjunto = obtenerDeRemitente(adjuntoId, remitenteId);
        if (adjunto.isEmpty()) {
            return false;
        }
        adjuntoDAO.eliminar(adjuntoId);
        Files.deleteIfExists(rutaFisica(adjunto.get()));
        return true;
    }

    // Toma solo el nombre del archivo guardado y lo busca en CARPETA.
    // Así funcionan las rutas viejas de Windows (C:\correo_adjuntos\...) y las nuevas de Docker.
    private static Path rutaFisica(Adjunto adjunto) {
        String ruta = adjunto.getRutaServidor();
        String nombre = ruta.substring(Math.max(ruta.lastIndexOf('\\'), ruta.lastIndexOf('/')) + 1);
        return CARPETA.resolve(nombre);
    }
}