#Documentation zur Plugin zum automatische TEI-Datei Erzeugung.

## Beschreibung

Das Plugin sucht unter die hinterlegten ECHO-XML-Dateien nach eine mit den gleichen MPIWG-Id. Dieser wird dann mittels ein hinterlegten XSL-Datein in einen TEI-Datei umwandlet, und in einen dafür definierten Ornder gespeichert. Danach wird der TEI-Datei in der Goobi-Prozess kopiert, nach der _source Unterordner im Goobi-Prozess image ordner. Dabei werden die Image links nach die images in Viewer ubersetzt. Von dort wird es automatisch exportiert (als "Download" Link), wenn das Prozess ins Viewer exportiert wird. 


## Installation und Konfiguration

Das Plugin besteht aus zwei Dateien:

```bash
goobi-plugin-step-tei-import.jar
plugin_intranda_step_tei-import.xml
```

Die Datei `"goobi-plugin-step-tei-import.jar"` enthält die Programmlogik und muss für den tomcat-Nutzer lesbar in folgendes Verzeichnis installiert werden:

```bash
/opt/digiverso/goobi/plugins/step/
```

Die Datei ```plugin_intranda_step_tei_import.xml``` muss ebenfalls für den tomcat-Nutzer lesbar sein und in folgendes Verzeichnis installiert werden:

```bash
/opt/digiverso/goobi/config/
```

Die Datei dient zur Konfiguration des Plugins und muss wie folgt aufgebaut sein:

```xml
<config_plugin>
    <config>
        <!-- which projects to use for (can be more then one, otherwise use *) -->
        <project>*</project>
        <step>*</step>
        <StepName>intranda_step_tei import</StepName>
        <ErrorMessage>TEI file could not be found.</ErrorMessage>

        <!--This is the path to the XSL file for transforming ECHO files into TEI files: -->
        <xslFile>/opt/digiverso/tei/info/echo2tei2.xsl</xslFile>
    
        <!--This is the base path to the TEI files: -->
        <teiFolder>/opt/digiverso/tei/</teiFolder>
    
        <!--This is the base path to the ECHo XML files: -->
        <echoFolder>/opt/digiverso/sftpupload/upload/uploads/echo_xml/</echoFolder>
        
        <!-- rulesets for the MM files: -->
        <rulesetPath>/opt/digiverso/goobi/rulesets/mpi.xml</rulesetPath>
        
        <!-- Viewer base path: the individual pages lie here + "id no."/"page no"/-->
        <viewerPages>https://mpiviewer.intranda.com/viewer/image/</viewerPages>

        <!--This is the base path to the Goobi MM files: -->
        <goobiMMFolder>/opt/digiverso/goobi/metadata/</goobiMMFolder>
    
    </config>
</config_plugin>
```

Eine Kopie liegt in dieser Repro, im Ordner "resources".

Im Element `"xslFile"`
wird der Pfad zur XSL-Datei hinterlegt.


Das Element `"teiFolder"`
definiert der Orner, wo die TEI-Dateien erzeugt werden.

Das Element `"echoFolder"`
definiert der Orner, wo die ECHO-XML-Dateien liegen.

Das Element `"rulesetPath"`
liefert der Pfad zur Ruleset für die MetsMods Dateien.

Das Element `"viewerPages"`
beschreibt die URL für die Viewer. 

Das Element `"goobiMMFolder"`
ist nur der standard metadata Ornder. Es ist nur für Testingzwecke änderbar.


## Einstellungen in Goobi

Nachdem das Plugin installiert und konfiguriert wurde, kann es innerhalb eines Arbeitsschrittes von Goobi genutzt werden.

Dazu muss innerhalb der gewünschten Aufgabe das Plugin `"plugin_intranda_step_tei_import"` eingetragen werden. Des Weiteren muss die Checkboxes Metadaten und Automatische Aufgabe gesetzt sein.

## Arbeitsweise

Die Arbeitsweise des Plugins innerhalb des korrekt konfigurierten Workflows sieht folgendermaßen aus:

* Wenn das Plugin innerhalb des Workflows aufgerufen wurde, öffnet es die METS-Datei.
* Die METS-Datei wird nach untersucht, ob es eine Metadatum "MPIWGID" hat, wenn ja, wird dieser als Id genommen.
* Die ECHO-XML-Datein im Echo Ordner werden durchsucht, nach eine Datei der ein Metadatum names "identifier", der dieser Id beinhaltet als String.
* Falls dieser Datei gefunden wird, wird es mittels der XSL-Datei in einer TEI-XML-Datei verwandelt. und im TEI-Ordner abgelegt.
* Danach wird der TEI-Datei in der Goobi-Prozess kopiert, nach der $Process-id$_source Unterordner im Goobi-Prozess image ordner. Dabei werden die Image links nach die images in Viewer ubersetzt. 
* Von dort wird es automatisch exportiert (als "Download" Link), wenn das Prozess ins Viewer exportiert wird. 
