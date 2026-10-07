@echo off
for %%s in (discovery-server user hotel payment notification minio api-gateway) do taskkill /F /T /IM cmd.exe /FI "WINDOWTITLE eq %%s*" >nul 2>&1

powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { $_.CommandLine -like '*JAVAMICROSERVICE*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }"

pushd user
call gradlew --stop
popd
