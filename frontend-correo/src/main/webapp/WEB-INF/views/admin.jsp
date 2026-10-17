<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="jakarta.json.JsonArray, jakarta.json.JsonObject, jakarta.json.JsonString, jakarta.json.JsonValue, java.util.List" %>
<%@ include file="parts/cabecera.jspf" %>
<%!
    static String esc(String t) {
        if (t == null) return "";
        return t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    static String txt(JsonObject o, String clave) {
        JsonValue v = o.get(clave);
        if (v == null || v.getValueType() == JsonValue.ValueType.NULL) return "";
        if (v.getValueType() == JsonValue.ValueType.STRING) return ((JsonString) v).getString();
        return v.toString();
    }

    static String[] columnas(String tabla) {
        switch (tabla) {
            case "usuarios": return new String[]{"id", "correo", "nombreCompleto", "estado", "cuotaAlmacenamientoMb"};
            case "carpetas": return new String[]{"id", "nombre", "esSistema"};
            case "adjuntos": return new String[]{"id", "nombreArchivo", "tamanoBytes", "fechaSubida"};
            default: return new String[]{"id", "mensajeId", "carpetaId", "tipoEnvio", "leido", "destacado"};
        }
    }
%>
<%
    String tabla = (String) request.getAttribute("tabla");
    String msg = (String) request.getAttribute("msg");
    String error = (String) request.getAttribute("error");
    JsonArray filas = (JsonArray) request.getAttribute("filas");
    Object detalleObj = request.getAttribute("detalle");
    JsonObject detalle = detalleObj instanceof JsonObject ? (JsonObject) detalleObj : null;
    List<String> tablas = (List<String>) request.getAttribute("tablas");
    String[] cols = columnas(tabla);
    String filtroId = request.getParameter("id") == null ? "" : request.getParameter("id");
%>

<h1 class="h4 mb-3">Administración · <%= esc(tabla) %></h1>

<ul class="nav nav-pills mb-3">
<% for (String t : tablas) { %>
    <li class="nav-item"><a class="nav-link<%= t.equals(tabla) ? " active" : "" %>" href="admin?tabla=<%= t %>"><%= t %></a></li>
<% } %>
</ul>

<% if (msg != null) { %><div class="alert alert-success"><%= esc(msg) %></div><% } %>
<% if (error != null) { %><div class="alert alert-danger"><%= esc(error) %></div><% } %>

<form method="get" action="admin" class="d-flex gap-2 mb-3">
    <input type="hidden" name="tabla" value="<%= tabla %>">
    <input class="form-control" type="text" name="id" placeholder="Filtrar por ID (UUID)" value="<%= esc(filtroId) %>">
    <button type="submit" class="btn btn-outline-secondary">Buscar</button>
    <a class="btn btn-outline-secondary" href="admin?tabla=<%= tabla %>">Ver todos</a>
</form>

<% if (detalle != null) { %>
<div class="card shadow-sm mb-4">
  <div class="card-body">
    <h2 class="h6">Registro</h2>
    <table class="table table-sm mb-4">
    <% for (String c : cols) { %>
        <tr><th class="w-25"><%= c %></th><td><%= esc(txt(detalle, c)) %></td></tr>
    <% } %>
    </table>

    <% if (!tabla.equals("adjuntos")) { %>
    <h3 class="h6">Actualizar</h3>
    <form method="post" action="admin" class="row g-2 mb-3">
        <input type="hidden" name="tabla" value="<%= tabla %>">
        <input type="hidden" name="accion" value="actualizar">
        <input type="hidden" name="id" value="<%= esc(txt(detalle, "id")) %>">
        <% if (tabla.equals("usuarios")) {
               String est = txt(detalle, "estado"); %>
            <div class="col-md-6">
                <label class="form-label">Nombre completo</label>
                <input class="form-control" name="nombreCompleto" value="<%= esc(txt(detalle, "nombreCompleto")) %>" required>
            </div>
            <div class="col-md-3">
                <label class="form-label">Estado</label>
                <select class="form-select" name="estado">
                    <option value="activo" <%= "activo".equals(est) ? "selected" : "" %>>activo</option>
                    <option value="suspendido" <%= "suspendido".equals(est) ? "selected" : "" %>>suspendido</option>
                    <option value="eliminado" <%= "eliminado".equals(est) ? "selected" : "" %>>eliminado</option>
                </select>
            </div>
            <div class="col-md-3">
                <label class="form-label">Cuota (MB)</label>
                <input class="form-control" type="number" min="1" name="cuotaAlmacenamientoMb" value="<%= esc(txt(detalle, "cuotaAlmacenamientoMb")) %>" required>
            </div>
        <% } else if (tabla.equals("carpetas")) { %>
            <div class="col-md-8">
                <label class="form-label">Nombre</label>
                <input class="form-control" name="nombre" value="<%= esc(txt(detalle, "nombre")) %>" required>
            </div>
        <% } else { %>
            <div class="col-md-6">
                <label class="form-label">ID de carpeta destino</label>
                <input class="form-control" name="carpetaId" value="<%= esc(txt(detalle, "carpetaId")) %>" required>
            </div>
            <div class="col-md-3 form-check mt-4">
                <input class="form-check-input" type="checkbox" name="leido" id="leido" <%= "true".equals(txt(detalle, "leido")) ? "checked" : "" %>>
                <label class="form-check-label" for="leido">Leído</label>
            </div>
            <div class="col-md-3 form-check mt-4">
                <input class="form-check-input" type="checkbox" name="destacado" id="destacado" <%= "true".equals(txt(detalle, "destacado")) ? "checked" : "" %>>
                <label class="form-check-label" for="destacado">Destacado</label>
            </div>
        <% } %>
        <div class="col-12"><button type="submit" class="btn btn-purple btn-sm">Guardar cambios</button></div>
    </form>
    <% } %>

    <form method="post" action="admin" onsubmit="return confirm('¿Eliminar este registro?');">
        <input type="hidden" name="tabla" value="<%= tabla %>">
        <input type="hidden" name="accion" value="eliminar">
        <input type="hidden" name="id" value="<%= esc(txt(detalle, "id")) %>">
        <button type="submit" class="btn btn-outline-danger btn-sm">Eliminar</button>
    </form>
  </div>
</div>
<% } %>

<% if (detalle == null && tabla.equals("adjuntos")) { %>
    <div class="alert alert-info">Los adjuntos se crean desde Redactar, al adjuntar archivos a un correo o borrador.</div>
<% } %>

<% if (detalle == null && !tabla.equals("adjuntos")) { %>
<details class="card shadow-sm mb-4 p-3">
    <summary class="fw-semibold">Crear nuevo registro</summary>
    <form method="post" action="admin" class="row g-2 mt-2">
        <input type="hidden" name="tabla" value="<%= tabla %>">
        <input type="hidden" name="accion" value="crear">
        <% if (tabla.equals("usuarios")) { %>
            <div class="col-md-6"><label class="form-label">Correo</label><input class="form-control" type="email" name="correo" required></div>
            <div class="col-md-6"><label class="form-label">Contraseña</label><input class="form-control" type="password" name="contrasena" required></div>
            <div class="col-md-6"><label class="form-label">Nombre completo</label><input class="form-control" name="nombreCompleto" required></div>
            <div class="col-md-3">
                <label class="form-label">Estado</label>
                <select class="form-select" name="estado">
                    <option value="activo">activo</option>
                    <option value="suspendido">suspendido</option>
                </select>
            </div>
            <div class="col-md-3"><label class="form-label">Cuota (MB)</label><input class="form-control" type="number" min="1" name="cuotaAlmacenamientoMb" value="1024"></div>
        <% } else if (tabla.equals("carpetas")) { %>
            <div class="col-md-8"><label class="form-label">Nombre</label><input class="form-control" name="nombre" required></div>
        <% } else { %>
            <div class="col-md-6"><label class="form-label">ID del mensaje</label><input class="form-control" name="mensajeId" required></div>
            <div class="col-md-6"><label class="form-label">ID de carpeta</label><input class="form-control" name="carpetaId" required></div>
            <div class="col-md-3">
                <label class="form-label">Tipo de envío</label>
                <select class="form-select" name="tipoEnvio">
                    <option value="para">para</option>
                    <option value="cc">cc</option>
                    <option value="cco">cco</option>
                </select>
            </div>
            <div class="col-md-3 form-check mt-4"><input class="form-check-input" type="checkbox" name="leido" id="nl"><label class="form-check-label" for="nl">Leído</label></div>
            <div class="col-md-3 form-check mt-4"><input class="form-check-input" type="checkbox" name="destacado" id="nd"><label class="form-check-label" for="nd">Destacado</label></div>
        <% } %>
        <div class="col-12"><button type="submit" class="btn btn-purple btn-sm">Crear</button></div>
    </form>
</details>
<% } %>

<% if (detalle == null) { %>
<div class="table-responsive">
<table class="table table-hover align-middle bg-white">
    <thead>
        <tr>
        <% for (String c : cols) { %><th><%= c %></th><% } %>
            <th></th>
        </tr>
    </thead>
    <tbody>
    <% if (filas != null) { for (JsonValue fila : filas) { JsonObject o = fila.asJsonObject(); %>
        <tr>
        <% for (String c : cols) { %><td><%= esc(txt(o, c)) %></td><% } %>
            <td class="text-nowrap">
                <a class="btn btn-sm btn-outline-secondary" href="admin?tabla=<%= tabla %>&id=<%= esc(txt(o, "id")) %>">Ver</a>
                <form method="post" action="admin" class="d-inline" onsubmit="return confirm('¿Eliminar este registro?');">
                    <input type="hidden" name="tabla" value="<%= tabla %>">
                    <input type="hidden" name="accion" value="eliminar">
                    <input type="hidden" name="id" value="<%= esc(txt(o, "id")) %>">
                    <button type="submit" class="btn btn-sm btn-outline-danger">Eliminar</button>
                </form>
            </td>
        </tr>
    <% } } %>
    </tbody>
</table>
</div>
<% } %>

<%@ include file="parts/pie.jspf" %>