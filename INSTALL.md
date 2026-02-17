# INTERLIS Validator Plugin für Apache Hop installieren

Diese Anleitung erklärt Ihnen, wie Sie die INTERLIS Validator Plugins in Apache Hop installieren. Die Installation ist einfach und erfordert keine technischen Kenntnisse.

## Voraussetzungen

- Apache Hop ist bereits installiert
- Sie wissen, wo Ihr Apache Hop Installationsverzeichnis liegt (im Folgenden "Hop-Verzeichnis" genannt)

---

## Schritt 1: Plugin-Dateien herunterladen

1. Gehen Sie auf die GitHub-Seite des Projekts
2. Klicken Sie links im Menü auf **Releases**
3. Laden Sie die beiden ZIP-Dateien herunter:
   - `hop-action-ilivalidator-*.zip` (für Workflow-Aktionen)
   - `hop-transform-ilivalidator-*.zip` (für Pipeline-Transformationen)

---

## Schritt 2: Hop-Verzeichnis öffnen

Öffnen Sie das Verzeichnis, in dem Apache Hop installiert ist. Das ist das Verzeichnis, in dem sich die `hop-gui.sh` (Mac/Linux) oder `hop-gui.bat` (Windows) Datei befindet.

**Typische Installationsorte:**

- **Windows:** `C:\Programme\apache-hop` oder `C:\Users\IhrName\apache-hop`
- **Mac:** `/Applications/apache-hop` oder `/Users/IhrName/apache-hop`
- **Linux:** `/opt/apache-hop` oder `/home/IhrName/apache-hop`

---

## Schritt 3: Plugin-Verzeichnis öffnen

Navigieren Sie im Hop-Verzeichnis zu folgendem Pfad:

```
plugins/
```

Falls das Verzeichnis `plugins` nicht existiert, erstellen Sie es einfach.

---

## Schritt 4: Action-Plugin installieren

1. Öffnen Sie im `plugins`-Verzeichnis das Verzeichnis `actions`
   - Pfad: `plugins/actions/`
   
2. Falls das Verzeichnis `actions` nicht existiert, erstellen Sie es

3. Erstellen Sie im `actions`-Verzeichnis einen neuen Ordner namens `ilivalidator`
   - Pfad: `plugins/actions/ilivalidator/`

4. Entpacken Sie die heruntergeladene Datei `hop-action-ilivalidator-*.zip`

5. Kopieren Sie den **gesamten Inhalt** des entpackten ZIP-Archivs in das Verzeichnis `plugins/actions/ilivalidator/`

   **Wichtig:** Kopieren Sie den Inhalt direkt in das Verzeichnis, nicht den gesamten Ordner aus dem ZIP. Die Struktur sollte danach so aussehen:
   
   ```
   plugins/actions/ilivalidator/
   ├── META-INF/
   ├── lib/
   └── plugins.xml
   ```

---

## Schritt 5: Transform-Plugin installieren

1. Öffnen Sie im `plugins`-Verzeichnis das Verzeichnis `transforms`
   - Pfad: `plugins/transforms/`
   
2. Falls das Verzeichnis `transforms` nicht existiert, erstellen Sie es

3. Erstellen Sie im `transforms`-Verzeichnis einen neuen Ordner namens `ilivalidator`
   - Pfad: `plugins/transforms/ilivalidator/`

4. Entpacken Sie die heruntergeladene Datei `hop-transform-ilivalidator-*.zip`

5. Kopieren Sie den **gesamten Inhalt** des entpackten ZIP-Archivs in das Verzeichnis `plugins/transforms/ilivalidator/`

   **Wichtig:** Kopieren Sie den Inhalt direkt in das Verzeichnis, nicht den gesamten Ordner aus dem ZIP. Die Struktur sollte danach so aussehen:
   
   ```
   plugins/transforms/ilivalidator/
   ├── META-INF/
   ├── lib/
   └── plugins.xml
   ```

---

## Schritt 6: Apache Hop neu starten

1. Falls Apache Hop gerade läuft, schließen Sie das Programm komplett
2. Starten Sie Apache Hop neu (Doppelklick auf `hop-gui.sh` bzw. `hop-gui.bat`)

---

## Schritt 7: Installation überprüfen

Nach dem Neustart von Apache Hop können Sie die Installation überprüfen:

### Für das Action-Plugin (Workflow):

1. Öffnen Sie einen Workflow-Editor
2. Klicken Sie mit der rechten Maustaste in die leere Fläche
3. Wählen Sie **Neue Aktion** → **Allgemein**
4. Sie sollten **INTERLIS Validator** in der Liste sehen

### Für das Transform-Plugin (Pipeline):

1. Öffnen Sie einen Pipeline-Editor
2. Klicken Sie mit der rechten Maustaste in die leere Fläche
3. Wählen Sie **Neue Transformation**
4. Sie sollten **INTERLIS Validator** in der Liste sehen

---

## Häufige Probleme

### Das Plugin wird nicht angezeigt

- **Hop neu starten:** Manchmal muss Hop mehrmals neu gestartet werden
- **Plugin-Verzeichnis prüfen:** Stellen Sie sicher, dass die Dateien direkt im `ilivalidator`-Verzeichnis liegen und nicht in einem Unterordner
- **Hop-Version:** Stellen Sie sicher, dass Sie eine kompatible Hop-Version verwenden (ab 2.17)

### Fehlermeldung beim Start

- **Java-Version prüfen:** INTERLIS Validator benötigt Java 17 oder höher
- **Log-Datei prüfen:** Im Hop-Verzeichnis gibt es eine `logs`-Ordner mit Protokolldateien

---

## Deinstallation

Falls Sie das Plugin wieder entfernen möchten:

1. Schließen Sie Apache Hop
2. Löschen Sie folgende Verzeichnisse:
   - `plugins/actions/ilivalidator/`
   - `plugins/transforms/ilivalidator/`
3. Starten Sie Apache Hop neu

---

## Hilfe

Bei weiteren Fragen oder Problemen:

- Öffnen Sie ein **Issue** auf GitHub
- Konsultieren Sie die [Apache Hop Dokumentation](https://hop.apache.org/)
