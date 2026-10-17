<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List, com.mycompany.frontend.correo.model.CorreoResumen, com.mycompany.frontend.correo.model.AdjuntoResumen" %>
<%@ include file="parts/cabecera.jspf" %>
<%
    CorreoResumen borrador = (CorreoResumen) request.getAttribute("borrador");
    String vPara = borrador != null ? (String) request.getAttribute("borradorPara")
            : (request.getParameter("para") != null ? request.getParameter("para") : "");
    String vCc = borrador != null ? (String) request.getAttribute("borradorCc")
            : (request.getParameter("cc") != null ? request.getParameter("cc") : "");
    String vCco = borrador != null ? (String) request.getAttribute("borradorCco")
            : (request.getParameter("cco") != null ? request.getParameter("cco") : "");
    String vAsunto = borrador != null ? borrador.asunto()
            : (request.getParameter("asunto") != null ? request.getParameter("asunto") : "");
    String vCuerpo = borrador != null ? borrador.cuerpo()
            : (request.getParameter("cuerpo") != null ? request.getParameter("cuerpo") : "");
    String vBorradorMensajeId = borrador != null ? borrador.mensajeId()
            : (request.getParameter("borradorMensajeId") != null ? request.getParameter("borradorMensajeId") : "");
    List<AdjuntoResumen> adjuntos = (List<AdjuntoResumen>) request.getAttribute("adjuntos");
%>

<h1 class="h4 mb-3"><%= borrador != null ? "Editar borrador" : "Nuevo correo" %></h1>

<% if (request.getAttribute("error") != null) { %>
    <div class="alert alert-danger"><%= CorreoResumen.esc((String) request.getAttribute("error")) %></div>
<% } %>

<form method="post" action="redactar" enctype="multipart/form-data" class="card shadow-sm p-3 p-md-4">
    <input type="hidden" name="borradorMensajeId" value="<%= CorreoResumen.esc(vBorradorMensajeId) %>">
    <div class="mb-3">
        <label class="form-label" for="para">Para</label>
        <input class="form-control" type="text" id="para" name="para"
               value="<%= CorreoResumen.esc(vPara) %>"
               placeholder="correo1@dominio.com, correo2@dominio.com">
    </div>
    <div class="mb-3">
        <label class="form-label" for="cc">CC</label>
        <input class="form-control" type="text" id="cc" name="cc"
               value="<%= CorreoResumen.esc(vCc) %>"
               placeholder="Opcional, separados por comas">
    </div>
    <div class="mb-3">
        <label class="form-label" for="cco">CCO</label>
        <input class="form-control" type="text" id="cco" name="cco"
               value="<%= CorreoResumen.esc(vCco) %>"
               placeholder="Opcional, separados por comas">
    </div>
    <div class="mb-3">
        <label class="form-label" for="asunto">Asunto</label>
        <input class="form-control" type="text" id="asunto" name="asunto" maxlength="255"
               value="<%= CorreoResumen.esc(vAsunto) %>">
    </div>
    <div class="mb-3">
        <label class="form-label" for="cuerpo">Mensaje</label>
        <textarea class="form-control" id="cuerpo" name="cuerpo" rows="10"><%= CorreoResumen.esc(vCuerpo) %></textarea>
    </div>

    <% if (adjuntos != null && !adjuntos.isEmpty()) { %>
    <div class="mb-3">
        <label class="form-label">Archivos ya adjuntos</label>
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

    <div class="mb-3">
        <label class="form-label" for="archivos">Adjuntar archivos (máximo 10 MB cada uno)</label>
        <input class="form-control" type="file" id="archivos" name="archivos" multiple>
    </div>
    <div class="d-flex flex-wrap gap-2 justify-content-end">
        <a href="bandeja" class="btn btn-outline-secondary">Cancelar</a>
        <button type="submit" name="guardarBorrador" value="1" formnovalidate
                class="btn btn-outline-purple">Guardar borrador</button>
        <button type="submit" class="btn btn-purple"><i class="bi bi-send"></i> Enviar</button>
    </div>
</form>

<%@ include file="parts/pie.jspf" %>