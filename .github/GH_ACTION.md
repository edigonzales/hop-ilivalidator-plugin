# Release workflow

`release.yml` prüft Pull Requests sowie Pushes auf `main`. Manuelle Läufe sind nur
für `main` zulässig. Der Build verwendet Java 21, Hop 2.19 und SWT unter Xvfb.

Vor der Veröffentlichung laufen `mvn clean verify`, die Prüfung der ZIP-Inhalte
und drei Pipelines mit den installierten Plugin-ZIPs in einer isolierten Hop-Installation.
Pull Requests veröffentlichen nichts.

Für `main` erzeugt der Release-Job wie bisher einen GitHub-Release mit beiden ZIPs.
Danach lädt er die Assets herunter, vergleicht ihre Bytes mit dem Build und wiederholt
den Paket- und Pipeline-Test. Der Workflow ist erst danach erfolgreich.

Nur der Release-Job benötigt `contents: write`. Es werden keine zusätzlichen Secrets
benötigt. Gleichzeitige Veröffentlichungen für `main` werden serialisiert.

Artefakte:
- `verification-diagnostics`: Testberichte und Pipeline-Logs, auch bei Fehlern.
- `release-zips`: ZIPs, Prüfsummen und Testergebnisse des Builds.
- `release-verification`: Ergebnisse des Downloadtests und tatsächliche Commons-Snapshots.
