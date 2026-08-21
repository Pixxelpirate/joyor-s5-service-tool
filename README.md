# Joyor Service-Tool

Universelles Selbst-Wartung, Diagnose & Referenz-Tool für alle gängigen **Joyor** E-Scooter.

## Unterstützte Modelle

S5 (ABE) · S7 · S8 · S10-S · S10-Z · F5S · F6 · F7 · A3 · A5 · Y5S · Y6S · Y8-S · Y10-S · X5S · LR8

## Features

- **Modellauswahl** — Alle gängigen Joyor-Modelle mit modellspezifischen Daten
- **USB + Bluetooth** — Dual-Mode-Verbindung zum Controller (Web Serial & Web Bluetooth)
- **Live-Daten** — Spannung, Geschwindigkeit, Temperatur, Strom, Fehler in Echtzeit
- **Fehlercodes** — Alle E01–E14 mit Schweregrad, Beschreibung und Behebungsanleitung
- **Wartungsplan** — 12 Wartungspunkte mit Intervallen, Kilometerstand-Tracking und localStorage
- **P-Settings** — Alle P01–P15 Parameter mit modellspezifischen Standardwerten und ABE-Warnhinweisen
- **Technische Daten** — Schnellreferenz, Spezifikationen und Anzugsmomente pro Modell
- **Fehlerbehebung** — 8 Schritt-für-Schritt-Anleitungen

## Installation

### Als Chrome-App (PWA)

1. Seite in Chrome (Android oder Desktop) aufrufen
2. Menu (⋮) → **App installieren** / **Zum Startbildschirm hinzufügen**
3. Das Tool startet als eigenständige App mit Offline-Support

### Als Android-APK

Das `android/`-Verzeichnis enthält ein fertiges Android-Studio-Projekt:

1. In Android Studio öffnen
2. Build → Generate Signed APK
3. APK auf dem Handy installieren

### Im Browser

Einfach `index.html` im Browser öffnen — funktioniert komplett offline als einzelne Datei.

## GitHub Pages

Die Seite kann direkt via GitHub Pages gehostet werden:

**Settings → Pages → Source: Deploy from branch → Branch: main → Save**

Danach erreichbar unter: `https://pixxelpirate.github.io/joyor-s5-service-tool/`

## Lizenz

Daten ohne Gewähr. Alle Arbeiten auf eigene Verantwortung.
