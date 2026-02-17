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

## Schritt 3: ZIP-Dateien entpacken

**Wichtig:** Die ZIP-Dateien enthalten bereits die korrekte Ordnerstruktur. Sie müssen die ZIP-Dateien einfach direkt im Hop-Verzeichnis entpacken.

### Action-Plugin installieren

1. Wechseln Sie in das Hop-Verzeichnis
2. Entpacken Sie die Datei `hop-action-ilivalidator-*.zip` **direkt im Hop-Verzeichnis**

Die ZIP-Datei erstellt automatisch folgende Ordnerstruktur:
```
Hop-Verzeichnis/
├── plugins/
│   └── actions/
│       └── ilivalidator/
│           └── hop-action-ilivalidator-*.jar
```

### Transform-Plugin installieren

1. Wechseln Sie in das Hop-Verzeichnis (falls nicht schon dort)
2. Entpacken Sie die Datei `hop-transform-ilivalidator-*.zip` **direkt im Hop-Verzeichnis**

Die ZIP-Datei erstellt automatisch folgende Ordnerstruktur:
```
Hop-Verzeichnis/
├── plugins/
│   └── transforms/
│       └── ilivalidator/
│           └── hop-transform-ilivalidator-*.jar
```

---

## Schritt 4: Apache Hop neu starten

1. Falls Apache Hop gerade läuft, schließen Sie das Programm komplett
2. Starten Sie Apache Hop neu (Doppelklick auf `hop-gui.sh` bzw. `hop-gui.bat`)

---

## Schritt 5: Installation überprüfen

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
- **Plugin-Verzeichnis prüfen:** Stellen Sie sicher, dass die JAR-Datei im richtigen Verzeichnis liegt:
  - Action: `plugins/actions/ilivalidator/hop-action-ilivalidator-*.jar`
  - Transform: `plugins/transforms/ilivalidator/hop-transform-ilivalidator-*.jar`
- **Hop-Version:** Stellen Sie sicher, dass Sie eine kompatible Hop-Version verwenden (ab 2.17)

### Fehlermeldung beim Start

- **Java-Version prüfen:** INTERLIS Validator benötigt Java 17 oder höher
- **Log-Datei prüfen:** Im Hop-Verzeichnis gibt es einen `logs`-Ordner mit Protokolldateien

### Falsche Entpackung unter Windows

Unter Windows passiert es manchmal, dass ZIP-Dateien falsch entpackt werden:

**Falsch:** Die ZIP erstellt einen zusätzlichen Ordner
```
Hop-Verzeichnis/
└── plugins/
    └── actions/
        └── ilivalidator/
            └── plugins/          ← falsch!
                └── actions/      ← falsch!
                    └── ilivalidator/
                        └── hop-action-ilivalidator-*.jar
```

**Lösung:** Verschieben Sie den Inhalt aus dem falschen Unterordner direkt nach `plugins/actions/ilivalidator/`

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
