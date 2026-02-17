# GitHub Workflows

Dieses Verzeichnis enthält GitHub Actions Workflows für das hop-ilivalidator-plugin Projekt.

## Verfügbare Workflows

### Create Draft Release (`release.yml`)

Erstellt ein Draft Release auf GitHub mit den gebauten Plugin-Assemblys.

**Auslöser:**
- Manuell über GitHub UI (Workflow Dispatch)

**Eingaben:**
- `version`: Versionsnummer (z.B. `0.1.0`)

**Ablauf:**
1. Code auschecken
2. Java 17 einrichten
3. Maven Build ausführen (erstellt die ZIP-Assemblys)
4. Tag erstellen (Format: `draft-{version}-{build-number}`)
5. Draft Release erstellen/aktualisieren
6. ZIP-Dateien als Release-Assets hochladen

**Verwendung:**

1. Gehen Sie auf GitHub zum Tab **Actions**
2. Wählen Sie links **Create Draft Release**
3. Klicken Sie auf **Run workflow**
4. Geben Sie die Versionsnummer ein (z.B. `0.1.0`)
5. Klicken Sie auf **Run workflow**

**Hinweis zu Draft Releases:**

- Draft Releases können beliebig oft aktualisiert werden
- Bei jedem Durchlauf wird das bestehende Draft Release mit dem gleichen Tag aktualisiert
- Die Assets werden automatisch überschrieben
- Erst beim Veröffentlichen (Publish) wird das Release final

**Nach dem Workflow:**

1. Gehen Sie zu **Releases** auf GitHub
2. Das Draft Release ist mit einem gelben **Draft**-Label markiert
3. Klicken Sie auf **Edit** um den Release-Text zu bearbeiten
4. Klicken Sie auf **Publish release** um das Release zu veröffentlichen
