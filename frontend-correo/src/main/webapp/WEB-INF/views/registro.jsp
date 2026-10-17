<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mycompany.frontend.correo.model.CorreoResumen" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Crear cuenta · Correo</title>
    <script>
        (function () {
            var tema = null;
            try { tema = localStorage.getItem('tema'); } catch (e) {}
            document.documentElement.setAttribute('data-bs-theme', tema || 'dark');
        })();
    </script>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/styles.css" rel="stylesheet">
</head>
<body class="d-flex align-items-center min-vh-100">
<div class="container py-4" style="max-width: 420px;">
    <div class="d-flex justify-content-end mb-2">
        <button type="button" id="btnTema" class="btn btn-outline-secondary btn-sm" title="Cambiar tema">
            <i class="bi bi-sun"></i>
        </button>
    </div>
    <div class="card shadow-sm p-4">
        <h1 class="h4 text-center mb-4 text-purple"><i class="bi bi-person-plus-fill"></i> Crear cuenta</h1>

        <% String error = (String) request.getAttribute("error");
           if (error != null) { %>
            <div class="alert alert-danger"><%= CorreoResumen.esc(error) %></div>
        <% } %>

        <form method="post" action="registro">
            <div class="mb-3">
                <label class="form-label" for="nombre">Nombre completo</label>
                <input class="form-control" type="text" id="nombre" name="nombre" required>
            </div>
            <div class="mb-3">
                <label class="form-label" for="correo">Correo</label>
                <input class="form-control" type="email" id="correo" name="correo" required>
            </div>
            <div class="mb-3">
                <label class="form-label" for="contrasena">Contraseña</label>
                <div class="input-group">
                    <input class="form-control" type="password" id="contrasena" name="contrasena"
                           minlength="6" required>
                    <button class="btn btn-outline-secondary" type="button"
                            onclick="alternar('contrasena', this)">Mostrar</button>
                </div>
            </div>
            <div class="mb-3">
                <label class="form-label" for="confirmar">Confirmar contraseña</label>
                <div class="input-group">
                    <input class="form-control" type="password" id="confirmar" name="confirmar"
                           minlength="6" required>
                    <button class="btn btn-outline-secondary" type="button"
                            onclick="alternar('confirmar', this)">Mostrar</button>
                </div>
            </div>
            <button type="submit" class="btn btn-purple w-100">Crear cuenta</button>
        </form>

        <p class="text-center small mt-3 mb-0">
            ¿Ya tienes cuenta? <a href="login">Inicia sesión</a>
        </p>
    </div>
</div>
<script>
    function alternar(id, boton) {
        var campo = document.getElementById(id);
        campo.type = campo.type === 'password' ? 'text' : 'password';
        boton.textContent = campo.type === 'password' ? 'Mostrar' : 'Ocultar';
    }

    (function () {
        var boton = document.getElementById('btnTema');
        function esOscuro() {
            return document.documentElement.getAttribute('data-bs-theme') === 'dark';
        }
        function pintarIcono() {
            boton.querySelector('i').className = esOscuro() ? 'bi bi-sun' : 'bi bi-moon-stars';
        }
        boton.addEventListener('click', function () {
            var nuevo = esOscuro() ? 'light' : 'dark';
            document.documentElement.setAttribute('data-bs-theme', nuevo);
            try { localStorage.setItem('tema', nuevo); } catch (e) {}
            pintarIcono();
        });
        pintarIcono();
    })();
</script>
</body>
</html>