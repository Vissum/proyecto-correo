<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, com.mycompany.frontend.correo.model.CorreoResumen" %>
<%@ include file="parts/cabecera.jspf" %>
<%
    String titulo = request.getAttribute("titulo") != null ? (String) request.getAttribute("titulo") : "Entrada";
    String destino = request.getAttribute("destino") != null ? (String) request.getAttribute("destino") : "leer";
    String error = (String) request.getAttribute("error");
%>

<h1 class="h4 mb-3"><%= CorreoResumen.esc(titulo) %></h1>

<% if (error != null) { %>
    <div class="alert alert-danger"><%= CorreoResumen.esc(error) %></div>
<% } %>

<div class="list-group shadow-sm">
<%
    List<CorreoResumen> correos = (List<CorreoResumen>) request.getAttribute("correos");
    if (correos == null || correos.isEmpty()) {
%>
    <div class="list-group-item text-muted">No hay correos en esta carpeta.</div>
<%
    } else {
        for (CorreoResumen c : correos) {
%>
    <a href="<%= destino %>?id=<%= c.enrutamientoId() %>&mensajeId=<%= c.mensajeId() %>"
       class="list-group-item list-group-item-action d-flex justify-content-between align-items-center <%= c.leido() ? "" : "fw-bold" %>">
        <div class="me-auto overflow-hidden">
           <div class="text-truncate"><% if (c.destacado()) { %><i class="bi bi-star-fill text-warning me-1"></i><% } %><%= CorreoResumen.esc(c.asunto()) %></div>
            <small class="text-muted"><%= CorreoResumen.esc(c.fecha()) %></small>
        </div>
        <% if (!c.leido()) { %><span class="badge bg-purple rounded-pill ms-2">Nuevo</span><% } %>
    </a>
<%
        }
    }
%>
</div>

<%@ include file="parts/pie.jspf" %>