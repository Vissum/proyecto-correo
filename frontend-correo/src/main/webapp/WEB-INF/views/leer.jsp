<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, com.mycompany.frontend.correo.model.CorreoResumen, com.mycompany.frontend.correo.model.AdjuntoResumen" %>
<%@ include file="parts/cabecera.jspf" %>

<a href="bandeja" class="btn btn-sm btn-outline-secondary mb-3"><i class="bi bi-arrow-left"></i> Volver</a>

<% CorreoResumen c = (CorreoResumen) request.getAttribute("correo");
   if (c == null) { %>
    <div class="alert alert-danger"><%= CorreoResumen.esc((String) request.getAttribute("error")) %></div>
<% } else { %>
<div class="card shadow-sm">
    <div class="card-body">
        <h2 class="h5"><%= CorreoResumen.esc(c.asunto()) %></h2>
        <p class="text-muted small mb-3"><%= CorreoResumen.esc(c.fecha()) %></p>
        <div style="white-space: pre-wrap;"><%= CorreoResumen.esc(c.cuerpo()) %></div>

        <form method="post" action="destacar" class="d-flex gap-2 mt-4">
            <input type="hidden" name="id" value="<%= CorreoResumen.esc(c.enrutamientoId()) %>">
            <input type="hidden" name="mensajeId" value="<%= CorreoResumen.esc(c.mensajeId()) %>">
            <button type="submit" name="destacado" value="true" class="btn btn-sm btn-outline-warning">
                <i class="bi bi-star"></i> Destacar
            </button>
            <button type="submit" name="destacado" value="false" class="btn btn-sm btn-outline-secondary">
                Quitar destacado
            </button>
        </form>

        <div class="d-flex flex-wrap gap-2 mt-3">
            <% String[][] acciones = {
                   {"Entrada", "bi-inbox", "Mover a Entrada"},
                   {"Archivo", "bi-archive", "Archivar"},
                   {"Spam", "bi-exclamation-octagon", "Marcar como spam"},
                   {"Papelera", "bi-trash", "Eliminar"}
               };
               for (String[] accion : acciones) { %>
            <form method="post" action="mover">
                <input type="hidden" name="id" value="<%= CorreoResumen.esc(c.enrutamientoId()) %>">
                <input type="hidden" name="carpeta" value="<%= accion[0] %>">
                <button type="submit" class="btn btn-sm btn-outline-secondary">
                    <i class="bi <%= accion[1] %>"></i> <%= accion[2] %>
                </button>
            </form>
            <% } %>
        </div>

        <% List<AdjuntoResumen> adjuntos = (List<AdjuntoResumen>) request.getAttribute("adjuntos");
           if (adjuntos != null && !adjuntos.isEmpty()) { %>
        <div class="mt-4">
            <h3 class="h6">Adjuntos</h3>
            <div class="list-group">
            <% for (AdjuntoResumen a : adjuntos) { %>
                <a class="list-group-item list-group-item-action d-flex justify-content-between align-items-center"
                   href="descargar?id=<%= a.id() %>">
                    <span><i class="bi bi-paperclip"></i> <%= CorreoResumen.esc(a.nombre()) %></span>
                    <small class="text-muted"><%= a.tamanoLegible() %></small>
                </a>
            <% } %>
            </div>
        </div>
        <% } %>
    </div>
</div>
<% } %>

<%@ include file="parts/pie.jspf" %>