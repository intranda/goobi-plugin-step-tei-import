---
title: TEI import
identifier: intranda_step_tei_import
published: false
description: Step Plugin zur Umwandlung von Dateien in eine TEI-Datei
---

## Einführung
Das Plugin sucht unter die hinterlegten ECHO-XML-Dateien nach eine mit den gleichen MPIWG-Id. Dieser wird dann mittels ein hinterlegten XSL-Datein in einen TEI-Datei umwandlet, und in einen dafür definierten Ordner gespeichert. Danach wird der TEI-Datei in der Goobi-Prozess kopiert, nach der _source Unterordner im Goobi-Prozess image ordner. Dabei werden die Image links nach die images in Viewer ubersetzt. Von dort wird es automatisch exportiert (als "Download" Link), wenn das Prozess ins Viewer exportiert wird. 

## Installation
Das Plugin besteht aus zwei Dateien:

```bash
goobi-plugin-step-tei-import.jar
plugin_intranda_step_tei-import.xml
```

Die Datei `goobi-plugin-step-tei-import.jar` enthält die Programmlogik und muss für den tomcat-Nutzer lesbar in folgendes Verzeichnis installiert werden:

```bash
/opt/digiverso/goobi/plugins/step/
```

Die Datei `plugin_intranda_step_tei_import.xml` muss ebenfalls für den tomcat-Nutzer lesbar sein und in folgendes Verzeichnis installiert werden:

```bash
/opt/digiverso/goobi/config/
```

## Überblick und Funktionsweise
Nachdem das Plugin installiert und konfiguriert wurde, kann es innerhalb eines Arbeitsschrittes von Goobi genutzt werden.

Dazu muss innerhalb der gewünschten Aufgabe das Plugin `"plugin_intranda_step_tei_import"` eingetragen werden. Des Weiteren muss die Checkboxes Metadaten und Automatische Aufgabe gesetzt sein.

![Konfiguration des Arbeitsschritts für die Nutzung des Plugins](screen1_de.png)

## Konfiguration
Die Konfiguration des Plugins erfolgt in der Datei `plugin_intranda_step_tei_import.xml` wie hier aufgezeigt:

{{CONFIG_CONTENT}}

{{CONFIG_DESCRIPTION_PROJECT_STEP}}

Parameter               | Erläuterung
------------------------|------------------------------------
`xslFile`               | wird der Pfad zur XSL-Datei hinterlegt. |
`teiFolder`             | definiert den Orner, wo die TEI-Dateien erzeugt werden. |
`echoFolder`            | definiert den Orner, wo die ECHO-XML-Dateien liegen.|
`rulesetPath`           | liefert der Pfad zur Ruleset für die MetsMods Dateien. |
`viewerPages`           | beschreibt die URL für die Viewer. |
`goobiMMFolder`         | ist nur der standard metadata Ornder. Es ist nur für Testingzwecke änderbar. |