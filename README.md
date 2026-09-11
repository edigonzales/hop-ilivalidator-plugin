# hop-ilivalidator-plugin

Apache Hop 2.19 plugin suite for INTERLIS validation.

## Modules

- `./ilivalidator-core`
  - Hop-independent validation core with ilivalidator integration and result model.
- `./hop-action-ilivalidator`
  - Workflow action plugin for single-file or folder validation with binary workflow result.
- `./hop-transform-ilivalidator`
  - Pipeline transform plugin with row-level output fields (`is_valid`, `validation_message`).
- `./assemblies/assemblies-action-ilivalidator`
  - Install ZIP for action plugin structure under `plugins/actions/ilivalidator`.
- `./assemblies/assemblies-transform-ilivalidator`
  - Install ZIP for transform plugin structure under `plugins/transforms/ilivalidator`.
- `./assemblies/debug`
  - Creates local debug layout in `./assemblies/debug/target/hop`.

## Build

Full build (tests + assemblies):

```bash
mvn clean verify
```

Fast local build (no tests, only core + plugins):

```bash
mvn -pl ilivalidator-core,hop-action-ilivalidator,hop-transform-ilivalidator -am -DskipTests package
```

Build prerequisites:

- Java 21 compatible toolchain (`maven.compiler.release=21`)
- Access to:
  - Maven Central for Apache Hop artifacts
  - `https://jars.interlis.ch/` for `ch.interlis:ilivalidator:1.15.0`
  - `https://jars.interlis.guru/snapshots/` for Commons `0.1.0-SNAPSHOT`

## Install in Hop

### Option A: Manual ZIP install

1. Build install ZIPs:

```bash
mvn -pl assemblies/assemblies-action-ilivalidator,assemblies/assemblies-transform-ilivalidator -am package
```

2. Extract into your Hop home:

```bash
unzip -o ./assemblies/assemblies-action-ilivalidator/target/hop-action-ilivalidator-0.1.0-SNAPSHOT.zip -d "$HOP_HOME"
unzip -o ./assemblies/assemblies-transform-ilivalidator/target/hop-transform-ilivalidator-0.1.0-SNAPSHOT.zip -d "$HOP_HOME"
```

3. Resulting plugin folders:
   - `$HOP_HOME/plugins/actions/ilivalidator`
   - `$HOP_HOME/plugins/transforms/ilivalidator`

### Option B: Scripted install into Hop home

```bash
./scripts/install-to-hop-home.sh "$HOP_HOME"
```

This script expects the assembly ZIPs from Option A to exist and then unpacks both ZIPs into `$HOP_HOME`.

## Shell scripts

### `scripts/dev-sync-hop-plugin.sh`

Builds plugin + assembly modules and syncs install ZIPs directly into `HOP_HOME`.

```bash
./scripts/dev-sync-hop-plugin.sh "$HOP_HOME"           # default target: suite
./scripts/dev-sync-hop-plugin.sh "$HOP_HOME" action
./scripts/dev-sync-hop-plugin.sh "$HOP_HOME" transform
```

Behavior:
- `suite` builds and installs both plugins
- `action` installs only `plugins/actions/ilivalidator`
- `transform` installs only `plugins/transforms/ilivalidator`
- target plugin folders are removed before unzip to avoid stale files

### `scripts/install-to-hop-home.sh`

Unpacks already-built assembly ZIPs into `HOP_HOME` (no Maven build step).

```bash
./scripts/install-to-hop-home.sh "$HOP_HOME"
```

### `scripts/dev-sync-debug.sh`

Optimized debug loop for the local debug Hop layout (`./assemblies/debug/target/hop` by default):
- builds `ilivalidator-core` + action/transform plugin jars
- removes known old jar patterns in debug plugin folders
- copies fresh plugin jars into debug Hop

## Debug layout

The debug module downloads `apache-hop-client-${hop.version}.zip` from the Apache archive and overlays plugin assemblies.

Output:

- `./assemblies/debug/target/hop`

Useful properties:

- `-Dhop.client.url=...` override client ZIP URL

Build debug layout once:

```bash
mvn -Pdebug -pl assemblies/debug -am -DskipTests package
```

Start Hop GUI from debug layout:

```bash
./assemblies/debug/target/hop/hop-gui.sh
```

## Fast development loop

You do not need to rebuild full assemblies and manually unzip/copy on every change.

1. Keep using the debug Hop from:
   - `./assemblies/debug/target/hop`
2. Rebuild only changed plugin modules + core and sync jars:

```bash
./scripts/dev-sync-debug.sh
```

Or sync directly into another Hop installation:

```bash
./scripts/dev-sync-debug.sh "$HOP_HOME"
```

This does:
- `mvn -pl ilivalidator-core,hop-action-ilivalidator,hop-transform-ilivalidator -am -DskipTests package`
- copies only these self-contained plugin jars into Hop plugin folders:
  - `hop-action-ilivalidator-<version>.jar`
  - `hop-transform-ilivalidator-<version>.jar`

3. Restart Hop GUI to load updated plugin classes.

Notes:
- No ZIP unpacking is needed for each code change.
- For dialog/UI tweaks, model mapping, service logic, this is usually fast enough.
- Java class reloading without restart is limited; in practice, restart Hop after plugin class changes.

If you change only one plugin module, you can build less:

```bash
mvn -pl hop-action-ilivalidator -am -DskipTests package
# or
mvn -pl hop-transform-ilivalidator -am -DskipTests package
```

### Optional: IDE Debug + HotSwap

For the shortest feedback loop:

1. Start Hop GUI in IDE debug mode with:
   - Main class `org.apache.hop.ui.hopgui.HopGui`
   - Working directory `./assemblies/debug/target/hop`
2. For small Java code edits (method body only), trigger IDE build and use HotSwap.
3. For signature/field/class changes, run `./scripts/dev-sync-debug.sh` and restart Hop.

## Troubleshooting

### `class antlr.CommonToken cannot be cast to class antlr.Token`

Cause: classloader conflict between Hop core libraries and plugin-bundled ANTLR classes.

For Hop 2.19 both plugin JARs relocate ANTLR to a private package. They no longer
rely on an ANTLR JAR being present in the Hop installation.

### Compiler/model resolution errors should fail the transform

Technical ilivalidator failures (for example `compiler failed`, unsupported INTERLIS version message, or model file resolution failures) are treated as technical errors and now throw a `HopTransformException` in the transform, independent of `failPipelineOnInvalid`.

## Recommended Hop GUI run configuration

- Main class: `org.apache.hop.ui.hopgui.HopGui`
- Working directory: `./assemblies/debug/target/hop`
- VM options (example): `-Dfile.encoding=UTF-8`

## Smoke tests

1. Workflow editor:
   - Add `INTERLIS Validator` action.
   - Validate a folder with mixed valid/invalid files.
   - Check workflow hop true/false behavior and optional result files.
2. Pipeline editor:
   - Feed file paths to `INTERLIS Validator` transform.
   - Verify output fields:
     - `is_valid`
     - `validation_message`

## Transfer file: configured value or incoming field

The transform's Input tab uses `ValueOrFieldControl` from Hop Plugin Commons.
Choose **Value / Variable** for a local file path (including `${VARIABLE}` expressions),
or **Input field** for a column from the preceding transform. The editable list loads
upstream metadata on first use; Refresh reloads it. Missing metadata never removes
a manually entered field name. Errors appear below the input without moving its label.

Both the configured path and field name survive mode changes and reopening the dialog.
They map to the existing `staticFilePath`, `filePathField`, and `useFilePathField`
properties. Only OK commits changes; Cancel, Escape, and window close discard them.
Existing `.hpl` files need no migration. Runtime interpretation is unchanged, including
the existing variable resolution of incoming paths.

Browse uses the native local-file picker because the validator accepts local paths.
Variables are resolved only to preselect a location; cancelling preserves the original
expression. Configuration, metaconfiguration, and log-directory controls are unchanged.

The transform depends on `ch.so.agi:hop-plugin-commons-ui:0.1.0-SNAPSHOT` from
`https://jars.interlis.guru/snapshots/`. Its ZIP includes UI and Core as separate JARs
in `plugins/transforms/ilivalidator/lib/`. Neither Commons nor Hop/SWT is embedded
in the shaded transform JAR. The action does not depend on Commons.

Hop 2.19 no longer supplies the ANTLR 2 classes previously used by the validator.
Both plugin JARs therefore include ANTLR relocated to
`ch.so.agi.ilivalidator.shaded.antlr`, avoiding collisions with host libraries.
The INTERLIS library versions remain unchanged.

## Maven artifacts and verification

The two installable ZIPs are published as normal Maven snapshot artifacts:

- `ch.so.agi:hop-action-ilivalidator:0.1.0-SNAPSHOT` (`zip`)
- `ch.so.agi:hop-transform-ilivalidator:0.1.0-SNAPSHOT` (`zip`)

Consumers declare the base `0.1.0-SNAPSHOT` version. Maven resolves the current
snapshot through repository metadata; timestamped snapshot filenames are not part
of this repository's dependency configuration.

The CI matrix uses Java 21 and 25 on Ubuntu, macOS and Windows. Ubuntu/Java 21 is
the canonical build: it creates the only publishable bundle, runs the package
checks and executes the Installed-Hop E2E. All other matrix cells run compatibility
tests only. Pull requests never publish Maven artifacts.

Run the local verification with Java 21:

```bash
mvn -U -B -ntp clean verify
python3 scripts/verify-packages.py
python3 scripts/run-e2e.py
```

On headless Linux, run Maven with `xvfb-run -a`. Maven selects the native SWT 3.134.0
artifact on Linux x86_64, Windows x86_64, and macOS ARM64/Intel, and automatically
uses `-XstartOnFirstThread` for macOS tests. The dialog integration tests cover real
OK/cancellation actions, upstream metadata, legacy metadata roundtrips, browse results,
and label/status geometry. The Commons repository retains its separate OS test matrix.

The E2E script downloads and checksums Hop 2.19 in `~/.cache/hop-ilivalidator`, installs
the ZIPs into `target/e2e/hop`, and runs three pipelines: two incoming file paths, one
configured path without input, and an invalid field name. Its INTERLIS model and data
are local fixtures. Logs, CSV output, and results stay under `target/e2e/`.
`target/package-verification.json` records archive checksums and the resolved base
Commons snapshots. Package verification compares the embedded Commons bytes with
the Maven-local artifacts selected by the current build; it does not manually select
timestamped repository versions.

An optional full debug distribution is built with `mvn -Pdebug -pl assemblies/debug -am package`.
The regular `verify` build creates only the two small plugin ZIPs. Development sync scripts
install those ZIPs including `lib/`; restart Hop after updating them.

The `CI` workflow verifies pull requests, pushes and manual runs. Pushes and manual
runs on `main` publish both ZIPs only after the complete matrix and canonical E2E
have passed. The publish job downloads the verified bundle, validates its manifest
and SHA-256 values, deploys without rebuilding, then resolves both public Maven
snapshots through an empty local cache and compares their bytes with the tested ZIPs.
The Maven credentials are supplied through `INTERLIS_MAVEN_USERNAME` and
`INTERLIS_MAVEN_TOKEN`.
