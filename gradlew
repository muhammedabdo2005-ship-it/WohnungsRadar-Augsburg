#!/usr/bin/env sh
set -e
cd "$(dirname "$0")"
if [ ! -f gradle/wrapper/gradle-wrapper.jar ]; then
  echo 'Bitte zuerst ./setup-wrapper.sh ausfuehren (Internet erforderlich).'
  exit 1
fi
exec java -cp gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain "$@"
