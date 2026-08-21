# Joyor S5 ABE Service-Tool

Selbst-Wartung, Diagnose & Referenz für den **Joyor S5 ABE** E-Scooter.

## Features

- **Bluetooth BLE** — Verbindung zum Scooter-Controller für Live-Daten (Spannung, Geschwindigkeit, Temperatur)
- **Fehlercodes** — Alle E01–E14 mit Schweregrad, Beschreibung und Behebungsanleitung
- **Wartungsplan** — 12 Wartungspunkte mit Intervallen, Kilometerstand-Tracking und localStorage
- **P-Settings** — Alle P01–P15 Parameter mit ABE-Warnhinweisen
- **Technische Daten** — Schnellreferenz, Spezifikationen und Anzugsmomente
- **Fehlerbehebung** — 8 Schritt-für-Schritt-Anleitungen

## Installation

### Als Chrome-App (PWA)

1. Seite in Chrome (Android oder Desktop) aufrufen
2. Menu (&#8942;) &rarr; **App installieren** / **Zum Startbildschirm hinzufügen**
3. Das Tool startet als eigenständige App mit Offline-Support

### Als Android-APK

Das `android/`-Verzeichnis enthält ein fertiges Android-Studio-Projekt:

1. In Android Studio öffnen
2. Build &rarr; Generate Signed APK
3. APK auf dem Handy installieren

### Im Browser

Einfach `index.html` im Browser öffnen — funktioniert komplett offline als einzelne Datei.

## GitHub Pages

Die Seite kann direkt via GitHub Pages gehostet werden:

**Settings &rarr; Pages &rarr; Source: Deploy from branch &rarr; Branch: main &rarr; Save**

Danach erreichbar unter: `https://pixxelpirate.github.io/joyor-s5-service-tool/`

## Technische Details

| | |
|---|---|
| Motor | 500 W BLDC |
| Akku | 48V 13Ah Li-Ion |
| V-Max (ABE) | 20 km/h |
| Reichweite | 40–55 km |
| Reifen | 10" Pneumatik |
| Bremsen | Scheibenbremse v/h |
| Zulassung | eKFV / ABE |

## Lizenz

Daten ohne Gewähr. Alle Arbeiten auf eigene Verantwortung.
