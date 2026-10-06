@echo off
start "discovery-server" cmd /k "cd /d discovery-server && gradlew bootRun"
timeout /t 30 /nobreak
for %%s in (user hotel booking payment notification minio) do start "%%s" cmd /k "cd /d %%s && gradlew bootRun"
timeout /t 30 /nobreak
start "api-gateway" cmd /k "cd /d api-gateway && gradlew bootRun"