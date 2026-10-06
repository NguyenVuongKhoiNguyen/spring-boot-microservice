$services = @{
  "discovery-server" = "cloud-eureka-server,actuator"
  "api-gateway"      = "cloud-gateway,cloud-eureka,actuator"
}

foreach ($s in $services.Keys) {
  $deps = $services[$s]
  $url = "https://start.spring.io/starter.tgz?type=gradle-project&language=java&javaVersion=21&groupId=com.poly&artifactId=$s&name=$s&baseDir=$s&dependencies=$deps"
  cmd /c "curl.exe -f -S -L `"$url`" | tar -xf -"
  Write-Host "Done: $s"
}

#powershell -ExecutionPolicy Bypass -File .\generate-infra.ps1