# hop-ilivalidator-plugin

Apache Hop 2.17 plugin suite for INTERLIS validation.

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

- Java 17 compatible toolchain (`maven.compiler.release=17`)
- Access to:
  - Maven Central for Apache Hop artifacts
  - `https://jars.interlis.ch/` for `ch.interlis:ilivalidator:1.15.0`

## Install in Hop

### Option A: Manual ZIP install

1. Build install ZIPs:

```bash
mvn -pl assemblies/assemblies-action-ilivalidator,assemblies/assemblies-transform-ilivalidator -am package
```

2. Extract into your Hop home:

```bash
unzip -o ./assemblies/assemblies-action-ilivalidator/target/assemblies-action-ilivalidator-0.1.0-SNAPSHOT.zip -d "$HOP_HOME"
unzip -o ./assemblies/assemblies-transform-ilivalidator/target/assemblies-transform-ilivalidator-0.1.0-SNAPSHOT.zip -d "$HOP_HOME"
```

3. Resulting plugin folders:
   - `$HOP_HOME/plugins/actions/ilivalidator`
   - `$HOP_HOME/plugins/transforms/ilivalidator`

### Option B: Scripted install into Hop home

```bash
./scripts/install-to-hop-home.sh "$HOP_HOME"
```

This script expects the assembly ZIPs from Option A to exist and then unpacks both ZIPs into `$HOP_HOME`.

## Debug layout

The debug module downloads `apache-hop-client-${hop.version}.zip` from the Apache archive and overlays plugin assemblies.

Output:

- `./assemblies/debug/target/hop`

Useful properties:

- `-Dhop.client.url=...` override client ZIP URL

Build debug layout once:

```bash
mvn -pl assemblies/debug -am -DskipTests package
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

Current fix in this project:
- plugin jars exclude `antlr:antlr`
- Hop core ANTLR (`/lib/core/org.apache.servicemix.bundles.antlr-2.7.7_5.jar`) is used consistently

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
