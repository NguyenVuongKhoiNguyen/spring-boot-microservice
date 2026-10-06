$services = "user","hotel","booking","payment","notification"

foreach ($s in $services) {
  $url = "https://start.spring.io/starter.tgz?type=gradle-project&language=java&javaVersion=21&groupId=com.poly&artifactId=$s&name=$s&baseDir=$s&dependencies=web,data-jpa,validation,actuator,cloud-eureka,postgresql,lombok"
  cmd /c "curl.exe -f -S -L `"$url`" | tar -xf -"
  Write-Host "Done: $s"
}

#powershell -ExecutionPolicy Bypass -File .\generate.ps1

