@echo off
cd /d "%~dp0"
echo Verificando Docker...
docker info >nul 2>&1
if errorlevel 1 (
  echo Docker Desktop no esta corriendo. Abrelo, espera a que diga "Engine running" y vuelve a ejecutar este archivo.
  pause
  exit /b 1
)
echo Levantando el proyecto. La primera vez tarda varios minutos.
docker compose up -d --build
if errorlevel 1 (
  echo ERROR al levantar los contenedores. Revisa el mensaje de arriba.
  pause
  exit /b 1
)
echo Esperando a que la aplicacion este lista...
powershell -NoProfile -Command "$u='http://localhost:8090/frontend-correo-1.0-SNAPSHOT/login'; for($i=0;$i -lt 60;$i++){ try { $r=Invoke-WebRequest -Uri $u -UseBasicParsing -TimeoutSec 3; if($r.StatusCode -eq 200){exit 0} } catch {}; Start-Sleep -Seconds 3 }; exit 1"
if errorlevel 1 (
  echo La aplicacion tardo demasiado. Revisa con: docker compose logs frontend
  pause
  exit /b 1
)
echo Listo. Abriendo la aplicacion...
start "" "http://localhost:8090/frontend-correo-1.0-SNAPSHOT/login"
pause