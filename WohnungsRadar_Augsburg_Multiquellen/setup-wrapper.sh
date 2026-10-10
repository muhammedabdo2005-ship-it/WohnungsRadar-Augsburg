#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p gradle/wrapper
curl -fsSL 'https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar' -o gradle/wrapper/gradle-wrapper.jar
EXPECTED="$(curl -fsSL 'https://services.gradle.org/distributions/gradle-8.7-wrapper.jar.sha256' | tr -d '\r\n')"
ACTUAL="$(shasum -a 256 gradle/wrapper/gradle-wrapper.jar | awk '{print $1}')"
if [[ "$EXPECTED" != "$ACTUAL" ]]; then rm -f gradle/wrapper/gradle-wrapper.jar; echo 'Prüfsumme stimmt nicht'; exit 1; fi
echo 'Gradle-Wrapper erfolgreich eingerichtet.'
