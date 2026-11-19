# CI and Maven publication

`ci.yml` prüft Pull Requests, Pushes auf `main` und manuelle Läufe. Der Build
verwendet Hop 2.19, Java 21/25 und SWT unter Xvfb auf Linux.

Die Matrix umfasst Ubuntu, macOS und Windows. Ubuntu/Java 21 führt `mvn clean verify`,
die ZIP-Prüfung und drei Pipelines mit den installierten Plugin-ZIPs in einer
isolierten Hop-Installation aus. Die übrigen Matrixläufe führen `mvn clean test` aus.
Pull Requests veröffentlichen nichts.

Für `main` lädt der Publish-Job das kanonische Bundle herunter und veröffentlicht
beide ZIPs auf `https://jars.interlis.guru/snapshots/`. Es werden keine GitHub-Releases
für die Plugins erzeugt. Maven veröffentlicht die getesteten Dateien direkt
aus diesem CI-Lauf.

Die Secrets heißen `INTERLIS_MAVEN_USERNAME` und `INTERLIS_MAVEN_TOKEN`.

Artefakte:
- `verification-diagnostics`: Testberichte und Pipeline-Logs, auch bei Fehlern.
- `hop-ilivalidator-plugin-canonical`: beide verifizierten ZIPs, koordinatenidentische
  Maven-POMs.
