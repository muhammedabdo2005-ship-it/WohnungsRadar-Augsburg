@echo off
cd /d "%~dp0"
if not exist "gradle\wrapper\gradle-wrapper.jar" (
  echo Bitte zuerst PowerShell ./setup-wrapper.ps1 ausfuehren.
  exit /b 1
)
java -cp "gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
