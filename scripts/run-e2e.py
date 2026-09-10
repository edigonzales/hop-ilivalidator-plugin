#!/usr/bin/env python3
"""Run installed ZIPs in an isolated Hop 2.19 distribution, never on Maven's classpath."""
import argparse, csv, hashlib, json, os, shutil, subprocess, urllib.request, zipfile
from pathlib import Path
import xml.etree.ElementTree as ET
from xml.sax.saxutils import escape

ROOT = Path(__file__).resolve().parents[1]
HOP_VERSION = '2.19.0'

def download(url, dest):
    with urllib.request.urlopen(url, timeout=120) as response, dest.open('wb') as out:
        shutil.copyfileobj(response, out)

def distribution(cache):
    cache.mkdir(parents=True, exist_ok=True)
    name = f'apache-hop-client-{HOP_VERSION}.zip'
    archive = cache / name
    checksum = cache / (name+'.sha512')
    if not checksum.exists():
        download(f'https://downloads.apache.org/hop/{HOP_VERSION}/{name}.sha512', checksum)
    if not archive.exists():
        temporary = archive.with_suffix('.part')
        download(f'https://mirror.init7.net/apache/hop/{HOP_VERSION}/{name}', temporary)
        temporary.replace(archive)
    expected = next(w.lower() for w in checksum.read_text().split() if len(w)==128)
    assert hashlib.sha512(archive.read_bytes()).hexdigest() == expected, 'Hop ZIP checksum mismatch'
    return archive

def transform(name, kind, body, x):
    return f'<transform><name>{name}</name><type>{kind}</type><copies>1</copies><distribute>Y</distribute>{body}<GUI><xloc>{x}</xloc><yloc>160</yloc></GUI></transform>'

def pipeline(work, fixtures, case):
    configured = case == 'configured'
    field = 'missing_path' if case == 'missing-field' else 'file_path'
    source = '' if configured else transform('Files', 'DataGrid', '''<fields><field><name>file_path</name><type>String</type><length>-1</length><precision>-1</precision></field></fields><data>'''+''.join(f'<line><item>{escape(str(fixtures / f))}</item></line>' for f in ('valid.xtf','second.xtf'))+'</data>', 100)
    validator = transform('Validate', 'INTERLIS_ILIVALIDATOR_TRANSFORM', f'''<useFilePathField>{'N' if configured else 'Y'}</useFilePathField><filePathField>{field}</filePathField><staticFilePath>${{FIXTURES}}/valid.xtf</staticFilePath><modelNames>TransferInputTest</modelNames><repositoryUrls>{escape(str(fixtures))}</repositoryUrls><configMode>STATIC</configMode><metaConfigMode>STATIC</metaConfigMode><failPipelineOnInvalid>Y</failPipelineOnInvalid><outputIsValidField>is_valid</outputIsValidField><outputValidationMessageField>validation_message</outputValidationMessageField>''', 340)
    output = transform('Results', 'TextFileOutput', f'''<separator>;</separator><enclosure>"</enclosure><header>Y</header><footer>N</footer><format>UNIX</format><encoding>UTF-8</encoding><compression>None</compression><file><name>{escape(str(work/case))}</name><extension>csv</extension><split>N</split><haspartno>N</haspartno><append>N</append><add_date>N</add_date><add_time>N</add_time><splitevery>0</splitevery></file><fields><field><name>is_valid</name><type>Boolean</type><format/></field><field><name>validation_message</name><type>String</type></field></fields>''', 570)
    hops = ('' if configured else '<hop><from>Files</from><to>Validate</to><enabled>Y</enabled></hop>')+'<hop><from>Validate</from><to>Results</to><enabled>Y</enabled></hop>'
    xml = f'<?xml version="1.0" encoding="UTF-8"?><pipeline><info><name>{case}</name><pipeline_type>Normal</pipeline_type><parameters><parameter><name>FIXTURES</name><default_value>{escape(str(fixtures))}</default_value></parameter></parameters></info><order>{hops}</order>{source}{validator}{output}</pipeline>'
    path=work/(case+'.hpl'); path.write_text(xml); return path

def main():
    p=argparse.ArgumentParser()
    p.add_argument('--transform', type=Path, default=ROOT/'assemblies/assemblies-transform-ilivalidator/target/hop-transform-ilivalidator-0.1.0-SNAPSHOT.zip')
    p.add_argument('--action', type=Path, default=ROOT/'assemblies/assemblies-action-ilivalidator/target/hop-action-ilivalidator-0.1.0-SNAPSHOT.zip')
    p.add_argument('--cache', type=Path, default=Path.home()/'.cache/hop-ilivalidator')
    args=p.parse_args()
    archive=distribution(args.cache)
    work=ROOT/'target/e2e'; shutil.rmtree(work, ignore_errors=True); work.mkdir(parents=True)
    with zipfile.ZipFile(archive) as z: z.extractall(work)
    hop=work/'hop'
    for package in (args.transform,args.action):
        with zipfile.ZipFile(package) as z: z.extractall(hop)
    for script in hop.glob('*.sh'): script.chmod(0o755)
    fixtures=work/'fixtures'; shutil.copytree(ROOT/'e2e/fixtures',fixtures)
    shutil.copyfile(fixtures/'valid.xtf', fixtures/'second.xtf')
    metadata=work/'config/metadata/pipeline-run-configuration'; metadata.mkdir(parents=True)
    (metadata/'local.json').write_text(json.dumps({'name':'local','engineRunConfiguration':{'Local':{'safe_mode':True,'rowset_size':'10000'}}}))
    env=os.environ.copy(); env.update({'HOP_CONFIG_FOLDER':str(work/'config'), 'HOP_AUDIT_FOLDER':str(work/'audit'), 'HOP_METADATA_FOLDER':str(metadata.parent),'FIXTURES':str(fixtures),'HOP_JAVA_HOME':env.get('JAVA_HOME','')})
    (work/'config').mkdir(exist_ok=True); (work/'audit').mkdir()
    results=[]
    for case in ('field','configured','missing-field'):
        hpl=pipeline(work, fixtures, case)
        cmd=['bash',str(hop/'hop-run.sh'),'-f',str(hpl),'-r','local']
        proc=subprocess.run(cmd,cwd=hop,env=env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,timeout=180)
        (work/(case+'.log')).write_text(proc.stdout)
        if case=='missing-field':
            assert proc.returncode != 0 and 'missing_path' in proc.stdout, proc.stdout[-6000:]
        else:
            assert proc.returncode==0, proc.stdout[-6000:]
            with (work/(case+'.csv')).open() as f: rows=list(csv.DictReader(f,delimiter=';'))
            assert len(rows)==(2 if case=='field' else 1), rows
            assert all(r['is_valid'].lower() in ('y','true') for r in rows), rows
        results.append({'case':case,'exitCode':proc.returncode,'result':'passed'})
    (work/'results.json').write_text(json.dumps(results,indent=2)+'\n'); print(json.dumps(results,indent=2))

if __name__=='__main__': main()
