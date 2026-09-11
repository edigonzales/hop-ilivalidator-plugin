# Repository instructions

## CI and tests

Before changing pipelines or test setup, read the
[shared CI contract](https://github.com/edigonzales/hop-plugin-ci/blob/main/docs/ci-contract.md).
The documentation follows `main`; use the interfaces at this repo's actual
workflow/helper revisions and preserve existing pins and `ci-ref` values.

Run the commands below from this repository root in Bash, using Python 3, Maven
and JDK 21 (`JAVA_HOME` and `PATH` pointing to that JDK). Compatibility jobs also
use JDK 25. For headless Linux SWT tests, run Maven under `xvfb-run -a`.
Set `HOP_CI_DIR` to an absolute checkout of `hop-plugin-ci` at the helper revision
used by this repo's workflow, then prepare the same Maven repositories as CI:

```bash
CI_TEST_TMP="$(mktemp -d)"
export MAVEN_SETTINGS="$CI_TEST_TMP/maven-settings.xml"
python3 "$HOP_CI_DIR/scripts/write_maven_settings.py" --output "$MAVEN_SETTINGS"
```

### Build, package checks and E2E

See [.github/workflows/verify.yml](.github/workflows/verify.yml) and
[.github/workflows/ci.yml](.github/workflows/ci.yml).

```bash
mvn -s "$MAVEN_SETTINGS" -U -B -ntp clean verify
python3 scripts/verify-packages.py
python3 scripts/run-e2e.py
```

Compatibility cells use `clean test`. Both scripts are canonical-only validation
commands, run before the shared workflow bundles the action and transform ZIPs.
The package checker reads the assembled ZIPs under
`assemblies/assemblies-action-ilivalidator/target` and
`assemblies/assemblies-transform-ilivalidator/target`.

E2E requires Bash and a usable JDK for `hop-run`, plus network access for the first
Hop distribution download. The script verifies/caches Apache Hop 2.19.0 under
`$HOME/.cache/hop-ilivalidator`, installs both ZIPs into `target/e2e/hop` and runs the
fixtures. It recreates `target/e2e` on every run. Use `--cache` for another cache
location and `--action` / `--transform` to select explicit package paths:

```bash
python3 scripts/run-e2e.py --action "$ACTION_ZIP" --transform "$TRANSFORM_ZIP"
```

Set `ACTION_ZIP` and `TRANSFORM_ZIP` to absolute paths of the packages being tested.
Publication in the CI workflow waits for verify and only runs on main for
non-PR events; it deploys the same canonical action/transform bundle.

The E2E script currently defaults to `0.1.0-SNAPSHOT` ZIP filenames, whereas the
package checker derives the POM version. When testing another version, pass both
ZIP paths explicitly. If Maven uses a non-default local repository, export
`MAVEN_REPO_LOCAL` to that same directory for package verification.
