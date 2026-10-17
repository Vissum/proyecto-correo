package com.mycompany.backend.correo.service;

import com.mycompany.backend.correo.dao.AdjuntoDAO;
import com.mycompany.backend.correo.dao.CarpetaDAO;
import com.mycompany.backend.correo.dao.EnrutamientoBuzonDAO;
import com.mycompany.backend.correo.dao.MensajeDAO;
import com.mycompany.backend.correo.dao.UsuarioDAO;
import com.mycompany.backend.correo.model.Carpeta;
import com.mycompany.backend.correo.model.EnrutamientoBuzon;
import com.mycompany.backend.correo.model.EnvioCorreo;
import com.mycompany.backend.correo.model.Mensaje;
import com.mycompany.backend.correo.model.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CorreoService {

    @Inject
    private MensajeDAO mensajeDAO;

    @Inject
    private EnrutamientoBuzonDAO enrutamientoBuzonDAO;

    @Inject
    private CarpetaDAO carpetaDAO;

    @Inject
    private UsuarioDAO usuarioDAO;

    @Inject
    private AdjuntoDAO adjuntoDAO;

    public Mensaje enviarCorreo(Mensaje mensaje, String correoDestinatario, String tipoEnvio) throws SQLException {
        Optional<Usuario> destinatarioOpt = usuarioDAO.obtenerPorCorreo(correoDestinatario);
        if (destinatarioOpt.isEmpty()) {
            throw new IllegalArgumentException("El destinatario con correo '" + correoDestinatario + "' no existe.");
        }
        Usuario destinatario = destinatarioOpt.get();

        Carpeta carpetaEnviados = carpetaDAO.obtenerPorUsuarioYNombre(mensaje.getRemitenteId(), "Enviados")
                .orElseThrow(() -> new IllegalStateException("No se encontro la carpeta 'Enviados' del remitente."));

        Carpeta carpetaEntrada = carpetaDAO.obtenerPorUsuarioYNombre(destinatario.getId(), "Entrada")
                .orElseThrow(() -> new IllegalStateException("No se encontro la carpeta 'Entrada' del destinatario."));

        Mensaje mensajeGuardado = mensajeDAO.crear(mensaje);

        EnrutamientoBuzon enrutamientoRemitente = new EnrutamientoBuzon();
        enrutamientoRemitente.setMensajeId(mensajeGuardado.getId());
        enrutamientoRemitente.setDestinatarioId(mensaje.getRemitenteId());
        enrutamientoRemitente.setCarpetaId(carpetaEnviados.getId());
        enrutamientoRemitente.setTipoEnvio("remitente");
        enrutamientoRemitente.setLeido(true);
        enrutamientoRemitente.setDestacado(false);
        enrutamientoBuzonDAO.crear(enrutamientoRemitente);

        EnrutamientoBuzon enrutamientoDestinatario = new EnrutamientoBuzon();
        enrutamientoDestinatario.setMensajeId(mensajeGuardado.getId());
        enrutamientoDestinatario.setDestinatarioId(destinatario.getId());
        enrutamientoDestinatario.setCarpetaId(carpetaEntrada.getId());
        enrutamientoDestinatario.setTipoEnvio(tipoEnvio != null ? tipoEnvio : "para");
        enrutamientoDestinatario.setLeido(false);
        enrutamientoDestinatario.setDestacado(false);
        enrutamientoBuzonDAO.crear(enrutamientoDestinatario);

        return mensajeGuardado;
    }

    public Mensaje enviarCorreoMultiple(EnvioCorreo envio) throws SQLException {
        if (envio.getRemitenteId() == null) {
            throw new IllegalArgumentException("El remitente es obligatorio.");
        }
        if (envio.getPara().isEmpty()) {
            throw new IllegalArgumentException("Debes indicar al menos un destinatario en Para.");
        }

        // Si viene de un borrador, debe pertenecer al remitente
        Mensaje borrador = null;
        if (envio.getBorradorMensajeId() != null) {
            borrador = mensajeDAO.obtenerPorId(envio.getBorradorMensajeId())
                    .filter(m -> envio.getRemitenteId().equals(m.getRemitenteId()))
                    .orElseThrow(() -> new IllegalArgumentException("El borrador no existe."));
        }

        Map<String, String> destinos = new LinkedHashMap<>();
        agregarDestinos(envio.getPara(), "para", destinos);
        agregarDestinos(envio.getCc(), "cc", destinos);
        agregarDestinos(envio.getCco(), "cco", destinos);

        Carpeta carpetaEnviados = carpetaDAO.obtenerPorUsuarioYNombre(envio.getRemitenteId(), "Enviados")
                .orElseThrow(() -> new IllegalStateException("No se encontro la carpeta 'Enviados' del remitente."));

        List<String> noExisten = new ArrayList<>();
        List<Destino> resueltos = new ArrayList<>();
        for (Map.Entry<String, String> entrada : destinos.entrySet()) {
            Optional<Usuario> usuarioOpt = usuarioDAO.obtenerPorCorreo(entrada.getKey());
            if (usuarioOpt.isEmpty()) {
                noExisten.add(entrada.getKey());
                continue;
            }
            Usuario usuario = usuarioOpt.get();
            if (usuario.getId().equals(envio.getRemitenteId())) {
                throw new IllegalArgumentException("No puedes enviarte el correo a ti mismo.");
            }
            Carpeta carpetaEntrada = carpetaDAO.obtenerPorUsuarioYNombre(usuario.getId(), "Entrada")
                    .orElseThrow(() -> new IllegalStateException(
                            "No se encontro la carpeta 'Entrada' de " + usuario.getCorreo() + "."));
            resueltos.add(new Destino(usuario, entrada.getValue(), carpetaEntrada));
        }
        if (!noExisten.isEmpty()) {
            throw new IllegalArgumentException("Estos destinatarios no existen: " + String.join(", ", noExisten));
        }

        Mensaje mensaje = new Mensaje();
        mensaje.setRemitenteId(envio.getRemitenteId());
        mensaje.setAsunto(envio.getAsunto() != null ? envio.getAsunto() : "");
        mensaje.setCuerpoPlano(envio.getCuerpoPlano() != null ? envio.getCuerpoPlano() : "");
        mensaje.setPaisOrigen(envio.getPaisOrigen() != null ? envio.getPaisOrigen() : "Guatemala");
        Mensaje mensajeGuardado = mensajeDAO.crear(mensaje);

        EnrutamientoBuzon copiaRemitente = new EnrutamientoBuzon();
        copiaRemitente.setMensajeId(mensajeGuardado.getId());
        copiaRemitente.setDestinatarioId(envio.getRemitenteId());
        copiaRemitente.setCarpetaId(carpetaEnviados.getId());
        copiaRemitente.setTipoEnvio("remitente");
        copiaRemitente.setLeido(true);
        copiaRemitente.setDestacado(false);
        enrutamientoBuzonDAO.crear(copiaRemitente);

        for (Destino d : resueltos) {
            EnrutamientoBuzon fila = new EnrutamientoBuzon();
            fila.setMensajeId(mensajeGuardado.getId());
            fila.setDestinatarioId(d.usuario().getId());
            fila.setCarpetaId(d.carpetaEntrada().getId());
            fila.setTipoEnvio(d.tipo());
            fila.setLeido(false);
            fila.setDestacado(false);
            enrutamientoBuzonDAO.crear(fila);
        }

        // Mover los adjuntos del borrador al correo enviado y borrar el borrador
        if (borrador != null) {
            adjuntoDAO.moverAMensaje(borrador.getId(), mensajeGuardado.getId());
            mensajeDAO.eliminar(borrador.getId());
        }

        return mensajeGuardado;
    }

    public Mensaje guardarBorrador(EnvioCorreo envio) throws SQLException {
        if (envio.getRemitenteId() == null) {
            throw new IllegalArgumentException("El remitente es obligatorio.");
        }
        String para = String.join(",", envio.getPara());
        String cc = String.join(",", envio.getCc());
        String cco = String.join(",", envio.getCco());
        String asunto = envio.getAsunto() != null ? envio.getAsunto() : "";
        String cuerpo = envio.getCuerpoPlano() != null ? envio.getCuerpoPlano() : "";

        // Actualizar un borrador existente
        if (envio.getMensajeId() != null) {
            Mensaje existente = mensajeDAO.obtenerPorId(envio.getMensajeId())
                    .filter(m -> envio.getRemitenteId().equals(m.getRemitenteId()))
                    .orElseThrow(() -> new IllegalArgumentException("El borrador no existe."));
            existente.setAsunto(asunto);
            existente.setCuerpoPlano(cuerpo);
            existente.setBorradorPara(para);
            existente.setBorradorCc(cc);
            existente.setBorradorCco(cco);
            mensajeDAO.actualizarBorrador(existente);
            return existente;
        }

        // Crear un borrador nuevo
        Carpeta carpetaBorradores = carpetaDAO.obtenerPorUsuarioYNombre(envio.getRemitenteId(), "Borradores")
                .orElseThrow(() -> new IllegalStateException("No se encontro la carpeta 'Borradores' del remitente."));

        Mensaje mensaje = new Mensaje();
        mensaje.setRemitenteId(envio.getRemitenteId());
        mensaje.setAsunto(asunto);
        mensaje.setCuerpoPlano(cuerpo);
        mensaje.setPaisOrigen(envio.getPaisOrigen() != null ? envio.getPaisOrigen() : "Guatemala");
        mensaje.setBorradorPara(para);
        mensaje.setBorradorCc(cc);
        mensaje.setBorradorCco(cco);
        Mensaje mensajeGuardado = mensajeDAO.crear(mensaje);

        EnrutamientoBuzon fila = new EnrutamientoBuzon();
        fila.setMensajeId(mensajeGuardado.getId());
        fila.setDestinatarioId(envio.getRemitenteId());
        fila.setCarpetaId(carpetaBorradores.getId());
        fila.setTipoEnvio("borrador");
        fila.setLeido(true);
        fila.setDestacado(false);
        enrutamientoBuzonDAO.crear(fila);

        return mensajeGuardado;
    }

    public List<EnrutamientoBuzon> obtenerBuzonPorCarpeta(UUID usuarioId, UUID carpetaId) throws SQLException {
        return enrutamientoBuzonDAO.listarPorCarpeta(usuarioId, carpetaId);
    }

    public List<EnrutamientoBuzon> buscar(UUID usuarioId, String texto) throws SQLException {
        return enrutamientoBuzonDAO.buscarPorTexto(usuarioId, texto);
    }

    public boolean cambiarEstadoLectura(UUID enrutamientoId, UUID usuarioId, boolean leido) throws SQLException {
        return enrutamientoBuzonDAO.marcarComoLeido(enrutamientoId, usuarioId, leido);
    }

    public boolean cambiarEstadoDestacado(UUID enrutamientoId, UUID usuarioId, boolean destacado) throws SQLException {
        return enrutamientoBuzonDAO.marcarComoDestacado(enrutamientoId, usuarioId, destacado);
    }

    public boolean moverMensajeDeCarpeta(UUID enrutamientoId, UUID usuarioId, UUID nuevaCarpetaId) throws SQLException {
        return enrutamientoBuzonDAO.moverDeCarpeta(enrutamientoId, usuarioId, nuevaCarpetaId);
    }

    public boolean eliminarEnrutamiento(UUID enrutamientoId, UUID usuarioId) throws SQLException {
        return enrutamientoBuzonDAO.eliminar(enrutamientoId, usuarioId);
    }

    public Optional<Mensaje> obtenerMensaje(UUID mensajeId) throws SQLException {
        return mensajeDAO.obtenerPorId(mensajeId);
    }

    private void agregarDestinos(List<String> correos, String tipo, Map<String, String> destinos) {
        for (String correo : correos) {
            String normalizado = correo.trim().toLowerCase();
            if (!normalizado.isEmpty()) {
                destinos.putIfAbsent(normalizado, tipo);
            }
        }
    }

    private record Destino(Usuario usuario, String tipo, Carpeta carpetaEntrada) {}
}