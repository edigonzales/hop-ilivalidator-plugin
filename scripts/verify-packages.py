#!/usr/bin/env python3
"""Inspect the actual ZIPs, including nested JAR contents and Commons provenance."""
import argparse, hashlib, io, json, urllib.request, xml.etree.ElementTree as ET, zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VERSION = '0.1.0-SNAPSHOT'

def default_zip(kind):
    return ROOT / f'assemblies/assemblies-{kind}-ilivalidator/target/hop-{kind}-ilivalidator-{VERSION}.zip'

def check(path, kind):
    with zipfile.ZipFile(path) as z:
        jars = [n for n in z.namelist() if n.endswith('.jar')]
        prefix = f'plugins/{"transforms" if kind == "transform" else "actions"}/ilivalidator/'
        expected = {prefix + f'hop-{kind}-ilivalidator-{VERSION}.jar'}
        if kind == 'transform':
            for module in ('core', 'ui'):
                matches = [n for n in jars if n.startswith(prefix + f'lib/hop-plugin-commons-{module}-')]
                assert len(matches) == 1, matches
                expected.add(matches[0])
        assert set(jars) == expected, (path, jars, expected)
        for name in jars:
            with zipfile.ZipFile(io.BytesIO(z.read(name))) as jar:
                classes = [n for n in jar.namelist() if n.endswith('.class')]
                assert not any(n.startswith(('org/apache/hop/', 'org/eclipse/swt/')) for n in classes), name
                if '/lib/' not in name:
                    assert not any(n.startswith(('ch/so/agi/hop/commons/', 'antlr/')) for n in classes), name
                    assert 'ch/so/agi/ilivalidator/shaded/antlr/LLkParser.class' in classes, name
        return {name: hashlib.sha256(z.read(name)).hexdigest() for name in jars}

def main():
    p = argparse.ArgumentParser()
    p.add_argument('--transform', type=Path, default=default_zip('transform'))
    p.add_argument('--action', type=Path, default=default_zip('action'))
    args = p.parse_args()
    report = {'packages': {}, 'commons': {}}
    for kind in ('action', 'transform'):
        path = getattr(args, kind)
        report['packages'][kind] = {'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'jars': check(path, kind)}
    # Match the packaged bytes to the published timestamp, not merely the mutable alias.
    with zipfile.ZipFile(args.transform) as z:
        for module in ('core', 'ui'):
            artifact = f'hop-plugin-commons-{module}'
            base = f'https://jars.interlis.guru/snapshots/ch/so/agi/{artifact}/{VERSION}/'
            name = next(n for n in z.namelist() if '/lib/'+artifact+'-' in n and n.endswith('.jar'))
            timestamp = Path(name).name[len(artifact)+1:-4]
            if timestamp == VERSION:
                metadata = ET.fromstring(urllib.request.urlopen(base+'maven-metadata.xml', timeout=60).read())
                timestamp = next(s.findtext('value') for s in metadata.findall('.//snapshotVersion')
                                 if s.findtext('extension') == 'jar' and s.find('classifier') is None)
            expected = urllib.request.urlopen(base+f'{artifact}-{timestamp}.jar.sha1', timeout=60).read().decode().split()[0]
            actual = hashlib.sha1(z.read(name)).hexdigest()
            assert actual == expected, f'{artifact}: packaged snapshot differs from published {timestamp}; rebuild with -U'
            report['commons'][artifact] = timestamp
    out = ROOT / 'target/package-verification.json'
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(report, indent=2)+'\n')
    print(json.dumps(report, indent=2))

if __name__ == '__main__': main()
