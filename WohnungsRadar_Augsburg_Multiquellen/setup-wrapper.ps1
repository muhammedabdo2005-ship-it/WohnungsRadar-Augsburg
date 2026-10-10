$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
New-Item -ItemType Directory -Force 'gradle/wrapper' | Out-Null
Invoke-WebRequest -UseBasicParsing 'https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle/wrapper/gradle-wrapper.jar'
$expected = (Invoke-WebRequest -UseBasicParsing 'https://services.gradle.org/distributions/gradle-8.7-wrapper.jar.sha256').Content.Trim()
$actual = (Get-FileHash 'gradle/wrapper/gradle-wrapper.jar' -Algorithm SHA256).Hash.ToLowerInvariant()
if ($expected -ne $actual) { Remove-Item 'gradle/wrapper/gradle-wrapper.jar'; throw 'Checksum mismatch' }
Write-Host 'Gradle wrapper eingerichtet.'
