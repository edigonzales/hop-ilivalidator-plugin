#!/usr/bin/env python3
"""Inspect both install ZIPs and verify normal Maven snapshot resolution."""

from __future__ import annotations

import argparse
import hashlib
import io
import json
import os
from pathlib import Path
import xml.etree.ElementTree as ET
import zipfile


ROOT = Path(__file__).resolve().parents[1]
POM_NAMESPACE = "{http://maven.apache.org/POM/4.0.0}"


def project_version() -> str:
    root = ET.parse(ROOT / "pom.xml").getroot()
    version = root.findtext(f"{POM_NAMESPACE}version") or root.findtext("version")
    if not version:
        raise SystemExit("Could not resolve project.version from pom.xml")
    return version


def sha256_bytes(value: bytes) -> str:
    return hashlib.sha256(value).hexdigest()


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def default_zip(kind: str, version: str) -> Path:
    return ROOT / f"assemblies/assemblies-{kind}-ilivalidator/target/hop-{kind}-ilivalidator-{version}.zip"


def validate_jar(name: str, content: bytes, expected_icon: str) -> None:
    with zipfile.ZipFile(io.BytesIO(content)) as jar:
        classes = [entry for entry in jar.namelist() if entry.endswith(".class")]
        assert not any(entry.startswith(("org/apache/hop/", "org/eclipse/swt/")) for entry in classes), name
        if "/lib/" not in name:
            assert not any(entry.startswith(("ch/so/agi/hop/commons/", "antlr/")) for entry in classes), name
            assert "ch/so/agi/ilivalidator/shaded/antlr/LLkParser.class" in classes, name
            assert expected_icon in jar.namelist(), f"{name}: missing {expected_icon}"
            assert not any(entry.endswith("/icons/xml-validator.svg") for entry in jar.namelist()), name


def check(path: Path, kind: str, version: str) -> dict[str, str]:
    expected_name = f"hop-{kind}-ilivalidator-{version}.zip"
    assert path.is_file(), path
    assert path.name == expected_name, (path.name, expected_name)
    with zipfile.ZipFile(path) as archive:
        assert archive.testzip() is None, path
        assert not any(name.endswith("/icons/xml-validator.svg") for name in archive.namelist()), path
        jars = [name for name in archive.namelist() if name.endswith(".jar")]
        prefix = f"plugins/{'transforms' if kind == 'transform' else 'actions'}/ilivalidator/"
        plugin_name = prefix + f"hop-{kind}-ilivalidator-{version}.jar"
        expected = {plugin_name}
        if kind == "transform":
            for module in ("core", "ui"):
                matches = [name for name in jars if name.startswith(prefix + f"lib/hop-plugin-commons-{module}-")]
                assert len(matches) == 1, matches
                expected.add(matches[0])
        assert set(jars) == expected, (path, jars, expected)
        icon = f"ch/so/agi/ilivalidator/hop/{'transform' if kind == 'transform' else 'action'}/icons/ilivalidator.svg"
        for name in jars:
            validate_jar(name, archive.read(name), icon)
        return {name: sha256_bytes(archive.read(name)) for name in jars}


def verify_commons_snapshot(transform_zip: Path, maven_repo: Path, version: str) -> dict[str, dict[str, str]]:
    report: dict[str, dict[str, str]] = {}
    with zipfile.ZipFile(transform_zip) as archive:
        for module in ("core", "ui"):
            artifact = f"hop-plugin-commons-{module}"
            matches = [
                name for name in archive.namelist()
                if f"/lib/{artifact}-" in name and name.endswith(".jar")
            ]
            assert len(matches) == 1, matches
            local = maven_repo / "ch" / "so" / "agi" / artifact / version / f"{artifact}-{version}.jar"
            assert local.is_file(), (
                f"Missing Maven-resolved {artifact}:{version} at {local}; "
                "build with Maven -U before package verification"
            )
            packaged = archive.read(matches[0])
            expected_hash = sha256_file(local)
            actual_hash = sha256_bytes(packaged)
            assert actual_hash == expected_hash, (
                f"{artifact}: packaged bytes differ from the resolved base snapshot "
                f"{version} ({actual_hash} != {expected_hash})"
            )
            report[artifact] = {
                "version": version,
                "sha256": actual_hash,
                "mavenLocalFile": str(local),
            }
    return report


def main() -> int:
    version = project_version()
    parser = argparse.ArgumentParser()
    parser.add_argument("--transform", type=Path, default=default_zip("transform", version))
    parser.add_argument("--action", type=Path, default=default_zip("action", version))
    parser.add_argument(
        "--maven-repo-local",
        type=Path,
        default=Path(os.environ.get("MAVEN_REPO_LOCAL", Path.home() / ".m2" / "repository")),
    )
    args = parser.parse_args()

    report = {
        "version": version,
        "packages": {},
        "commons": {},
    }
    for kind in ("action", "transform"):
        path = getattr(args, kind)
        report["packages"][kind] = {
            "zipFile": str(path),
            "sha256": sha256_file(path),
            "jars": check(path, kind, version),
        }
    report["commons"] = verify_commons_snapshot(args.transform, args.maven_repo_local, version)

    out = ROOT / "target/package-verification.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
