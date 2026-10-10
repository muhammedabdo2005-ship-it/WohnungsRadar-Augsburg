# WohnungsRadar Augsburg – Multi-Quellen-App (Android-Projekt v0.2)

**Status:** Vollständiger Android-*Quellcode*, aber **keine fertige APK**. Das Projekt wurde hier mangels Android-SDK **nicht auf einem Android-Gerät kompiliert/getestet**. Das Projekt ist ein funktionsfähiger Prototyp-Entwurf, keine garantierte Rundum-Überwachung des gesamten Immobilienmarkts.

## Was diese Version tatsächlich macht

1. **ImmoScout24**, **immowelt** und **Kleinanzeigen**: Sobald die offizielle Android-App eine passende neue Suchalarm-Push-Benachrichtigung zeigt, kann WohnungsRadar die sichtbaren Informationen lokal speichern und zusätzlich als WohnungsRadar-Meldung anzeigen. **Beide Apps müssen installiert sein; Suchaufträge und Push-Alarme werden auf der jeweiligen Plattform eingerichtet.**
2. **Regionale Anbieter per E-Mail**: Wenn Gmail, Outlook, Samsung Mail oder K-9 Mail in einer sichtbaren Android-Benachrichtigung den Anbieter und neue Immobilienangebote erkennen lassen, kann die App auch diese Meldung übernehmen. Unterstützte Namen im Erkennungsfilter: Augsburger Allgemeine/EXTRA, WSB Bayern, Wohnbaugruppe Augsburg, Dawonia/Immomio, JÄGER Wohnimmobilien. **Das ist keine Garantie, dass diese Anbieter überhaupt entsprechende Benachrichtigungsmails versenden.**
3. **Weitere Webseiten**: Über Android **Teilen** oder „Inserat hinzufügen“ kann jeder HTTPS-Inserat-Link lokal gespeichert werden.
4. **Echte RSS- oder Atom-Feeds**: Wenn ein Makler tatsächlich eine Feed-URL veröffentlicht, kann man sie eintragen. Android prüft solche Feeds mittels JobScheduler **ungefähr alle 15 Minuten**, u. U. deutlich später durch Akku-/Energiesparfunktionen. Beim ersten Abruf vorhandene Einträge importieren, aber nicht alle einzeln mit Push bombardieren.
5. **Kontaktfunktion:** Ein Bewerbungsanschreiben lässt sich einmal speichern. Bei jeder Anzeige kannst du „Anschreiben kopieren + Inserat öffnen“ auswählen und die Nachricht anschließend im **Originalportal** selbst einfügen und abschicken. Bereits kontaktierte Anbieter lassen sich markieren.
6. **Dubletten vermeiden:** Wiederholte identische Meldungen und bereits erfasste Links werden weitgehend zusammengefasst. Alles wird ausschließlich in der privaten Android-Datenbank gespeichert.

### Was diese Version NICHT kann

- Sie durchsucht **nicht automatisch sämtliche Websites** oder jeden Makler in Augsburg; es gibt keine einheitliche offizielle Schnittstelle.
- Sie kann ohne erteilten Benachrichtigungszugriff und ohne vorher eingerichtete Original-Suchaufträge keine neuen Plattformangebote sehen.
- **Augsburg + 100 km und mindestens 4 Zimmer** werden nicht durch Geolocation im Code erzwungen. Die Suche muss in jedem Anbieter selbst entsprechend eingestellt werden. Textuelle Angaben unter 4 Zimmern werden teilweise aussortiert.
- Die App kann **kein Eins-zu-eins-Deep-Link** versprechen, wenn Android-Originalbenachrichtigungen keine URL enthalten oder der Benachrichtigungstoken bereits abgelaufen ist. Dann öffnet sie die Anbieter-App bzw. deren Suchseite.
- Nicht automatisch beim Vermieter bewerben. Keine Kontopasswörter speichern oder API-Sperren umgehen.
- Keine sekunden- oder echtzeitgenaue Garantie. RSS wird durch Android periodisch geplant.

## Einrichtung für Android (Computer mit Android Studio benötigt)

1. ZIP-Datei entpacken. Auf Windows, macOS oder Linux **Android Studio** und ein Android-SDK **API 35** installieren, Gradle JDK **17** oder kompatibles JDK konfigurieren.
2. Einmalig Gradle-Wrapper einrichten (Internet): Windows PowerShell im Projektverzeichnis `./setup-wrapper.ps1`, auf macOS/Linux Terminal `./setup-wrapper.sh`. Der Befehl lädt die Wrapper-JAR vom **offiziellen Gradle-GitHub** und überprüft den **SHA-256-Hash** gegen die offizielle Gradle-Distribution. Wenn diese Seiten nicht erreichbar sind, muss der Wrapper auf einem Computer mit Gradle 8.7 mit `gradle wrapper --gradle-version 8.7` generiert werden.
3. Ordner `WohnungsRadar_Augsburg_Multiquellen` in Android Studio über **Open** öffnen. Auf vollständige Gradle-Synchronisierung prüfen. (Projekt: Android Gradle Plugin 8.6.1, Gradle 8.7, Java 17, Android min API 26.)
4. **Build > Build APK(s)**; die Debug-APK liegt normalerweise unter `app/build/outputs/apk/debug/app-debug.apk`.
5. APK nur auf dem **eigenen Android-Gerät** installieren. Android erlaubt die Installation ggf. erst nach deiner Freigabe. Die Debug-App ist nicht Google-Play-verifiziert.
6. In WohnungsRadar **Benachrichtigungszugriff** und **eigene Push-Meldungen** aktivieren. Für RSS-Hintergrundabrufe je nach Gerät die Akku-Optimierung prüfen.
7. Bei ImmoScout24, immowelt und Kleinanzeigen jeweils Suchauftrag für Mietwohnung und separat Miethaus erstellen, mit **mindestens 4 Zimmern**, Augsburg und verfügbarem Radius. Falls maximal 50 km möglich: weitere Suchaufträge um umliegende Orte speichern. Alarme einschalten.
8. Bei der Augsburger Allgemeinen, sofern verfügbar, den **„sofort“**-E-Mail-Suchalarm aktivieren. E-Mail-App muss im Android-Benachrichtigungstext erkennbar machen, von wem eine neue Immobilienmail stammt.
9. Bewerbungstext in WohnungsRadar hinterlegen (Platzhalter durch echte Angaben ersetzen). Bei einer Anzeige auf „kopieren + öffnen“ und im Portal einfügen.

## Anbieter-Startpunkte

- ImmoScout24: https://www.immobilienscout24.de/
- immowelt: https://www.immowelt.de/
- Kleinanzeigen: https://www.kleinanzeigen.de/
- Augsburger Allgemeine Immobilien: https://immobilien.augsburger-allgemeine.de/
- WSB Bayern Mietangebote: https://wsb-bayern.de/mietangebote/
- Wohnbaugruppe Augsburg Interessentenformular: https://www.wohnbaugruppe.de/leben/immobilien/interessentenformular
- Dawonia: https://www.dawonia.de/de/mieten
- JÄGER Wohnimmobilien Suchauftrag: https://jaeger-wohnimmobilien.de/suchauftrag/

**Achtung:** Bei der Wohnbaugruppe ist die Interessentenregistrierung der vorgesehene Weg; ihre Mieter-App ist kein Wohnungsfinder für neue Interessenten. Bei Dawonia werden Anfragen über Immomio abgewickelt. Bei WSB ist derzeit hier keine zuverlässige RSS/API/Suchalarm-Schnittstelle für die App implementiert.

## Daten und Privatsphäre

„Benachrichtigungszugriff“ ist eine starke Android-Berechtigung: Android könnte der App theoretisch Benachrichtigungen aller Apps bereitstellen. **Der implementierte Listener filtert unmittelbar** auf die Immobilien-App-Pakete bzw. E-Mail-Apps mit eindeutiger Immobilienanbieter-Nennung. Kein Upload, keine Analytics, kein Tracking, keine Passwörter. Die App verwendet das Internet nur für **selbst eingetragene RSS-Feed-URLs** und zum Öffnen der offiziellen Websites. Die Bewerbungsnachricht bleibt lokal. Die Datenbank wird mit der Deinstallation entfernt (Backup deaktiviert).

## Testen und Grenzen des Entwicklungsstands

Ein reiner Java-Test der Hilfsfunktionen liegt unter `tests/TestListingUtils.java`; ausführbar mit `javac` und `java` ohne Android-SDK. Die Android-App selbst wurde noch nicht gebaut oder auf echtem Smartphone getestet. Platformänderungen können Anpassungen erfordern.

## Weitere Erweiterung (nicht im Prototyp)

Eine echte zusätzliche automatische Webseitenüberwachung ist nur pro Quelle möglich, wenn die Plattform eine zulässige API, öffentliche RSS-/Atom-Feeds, einen E-Mail-Suchalarm oder eine andere ausdrücklich unterstützte Anbindung anbietet. Für dauerhafte sekundennahe Überwachung wäre ein dauerhaft erreichbarer Server und eine rechtmäßige Datenquelle nötig.

## Alternative: APK über GitHub Actions bauen (auch ohne eigenen Computer)

Unter `.github/workflows/build-apk.yml` liegt ein automatisierter Build. Wenn du den **entpackten Projektinhalt** in ein eigenes privates GitHub-Repository hochlädst, kannst du in **Actions > Android APK erstellen > Run workflow** den Build auslösen. Nach erfolgreichem Lauf liegt eine Debug-APK im Actions-Artefakt **WohnungsRadar-Debug-APK**. Dafür ist ein GitHub-Konto und eine Einrichtung des Repositorys nötig; das ist kein vorab verifiziertes APK-Build-Ergebnis. Prüfe die Berechtigungen beim Installieren von selbst erstellten APKs.
