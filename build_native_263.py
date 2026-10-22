import os
import glob
import subprocess
import zipfile
import hashlib

def main():
    root = os.path.abspath("D:/GEMINI AGY/workspaces/litematica-printer-pro")
    os.chdir(root)

    mc_dir = os.path.expandvars('%APPDATA%/.minecraft')
    libs_dir = os.path.join(mc_dir, 'libraries')
    mods_dir = os.path.join(mc_dir, 'mods')
    version_jar = os.path.join(mc_dir, 'versions', '26.3', '26.3.jar')

    cp_jars = [version_jar]
    for f in glob.glob(os.path.join(libs_dir, '**', '*.jar'), recursive=True):
        cp_jars.append(f)
    for f in glob.glob(os.path.join(mods_dir, '*.jar')):
        if 'printer' not in f.lower():
            cp_jars.append(f)
    for f in glob.glob(os.path.join(mc_dir, '.fabric', 'processedMods', '*.jar')):
        cp_jars.append(f)

    classpath = os.pathsep.join(cp_jars)
    javac = os.path.expandvars('%USERPROFILE%/AppData/Local/Packages/Microsoft.4297127D64EC6_8wekyb3d8bbwe/LocalCache/Local/runtime/java-runtime-epsilon/windows-x64/java-runtime-epsilon/bin/javac.exe')

    os.makedirs('tmp_build', exist_ok=True)
    with open('tmp_build/cp.txt', 'w', encoding='utf-8') as f:
        f.write('-cp\n' + classpath + '\n')

    # Find all java source files
    src_files = []
    for r, d, files in os.walk('src/main/java'):
        for file in files:
            if file.endswith('.java'):
                src_files.append(os.path.join(r, file).replace('\\', '/'))

    with open('tmp_build/sources.txt', 'w', encoding='utf-8') as f:
        f.write('\n'.join(src_files) + '\n')

    out_classes = os.path.join(root, 'build', 'classes', 'java', 'main')
    os.makedirs(out_classes, exist_ok=True)

    print(f"Compiling {len(src_files)} Java files for Minecraft 26.3...")
    cmd = [
        javac,
        '@tmp_build/cp.txt',
        '@tmp_build/sources.txt',
        '-d', out_classes,
        '-encoding', 'UTF-8',
        '--release', '21'
    ]
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode != 0:
        print("Compilation FAILED:")
        print(res.stderr)
        return False
    print("Compilation SUCCESSFUL!")

    # Now package the JAR
    jar_path_versioned = os.path.join(root, 'build', 'libs', 'litematica-printer-pro-26.3-1.0.0.jar')
    jar_path_unversioned = os.path.join(root, 'build', 'libs', 'litematica-printer-pro-1.0.0.jar')
    mc_mods_jar = os.path.join(mods_dir, 'litematica-printer-pro-26.3-1.0.0.jar')
    os.makedirs(os.path.dirname(jar_path_versioned), exist_ok=True)

    with zipfile.ZipFile(jar_path_versioned, 'w', zipfile.ZIP_DEFLATED) as zf:
        # Add class files
        for r, d, files in os.walk(out_classes):
            for file in files:
                full_path = os.path.join(r, file)
                rel_path = os.path.relpath(full_path, out_classes).replace('\\', '/')
                zf.write(full_path, rel_path)

        # Add resources
        res_dir = os.path.join(root, 'src', 'main', 'resources')
        for r, d, files in os.walk(res_dir):
            for file in files:
                full_path = os.path.join(r, file)
                rel_path = os.path.relpath(full_path, res_dir).replace('\\', '/')
                zf.write(full_path, rel_path)

        # Add LICENSE
        license_path = os.path.join(root, 'LICENSE')
        if os.path.exists(license_path):
            zf.write(license_path, 'LICENSE_litematica-printer-pro')

    import shutil
    shutil.copyfile(jar_path_versioned, jar_path_unversioned)
    shutil.copyfile(jar_path_versioned, mc_mods_jar)

    # Compute checksum
    h = hashlib.sha256(open(jar_path_versioned, 'rb').read()).hexdigest()
    with open(os.path.join(root, 'build', 'libs', 'CHECKSUMS.txt'), 'w', encoding='utf-8') as f:
        f.write(f"{h}  litematica-printer-pro-1.0.0.jar\n")
        f.write(f"{h}  litematica-printer-pro-26.3-1.0.0.jar\n")

    print(f"Packaged JAR successfully! SHA256: {h}")
    print(f"Deployed to: {mc_mods_jar}")
    return True

if __name__ == '__main__':
    main()
