import os
import sys
import zipfile
import hashlib

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    workspace_dir = os.path.dirname(script_dir)
    jar_path = os.path.join(workspace_dir, "build", "libs", "litematica-printer-pro-1.0.0.jar")
    checksums_path = os.path.join(workspace_dir, "build", "libs", "CHECKSUMS.txt")

    print(f"Inspecting JAR bundle: {jar_path}")
    if not os.path.exists(jar_path):
        print(f"Error: JAR file not found at {jar_path}")
        sys.exit(1)

    required_entries = [
        "fabric.mod.json",
        "assets/litematica_printer_pro/icon.png",
        "assets/litematica_printer_pro/lang/en_us.json",
        "com/slopggobbler/litematica/LitematicaPrinterProMod.class",
        "com/slopggobbler/litematica/compat/ModMenuIntegration.class"
    ]

    print("Verifying ZIP structure and mandatory entries...")
    with zipfile.ZipFile(jar_path, 'r') as zf:
        namelist = zf.namelist()
        for entry in required_entries:
            if entry not in namelist:
                print(f"Error: Missing required entry '{entry}' inside JAR!")
                sys.exit(1)
            else:
                print(f"  [OK] Found entry: {entry}")

    print("Verifying SHA-256 checksum...")
    if not os.path.exists(checksums_path):
        print(f"Error: Checksums file not found at {checksums_path}")
        sys.exit(1)

    sha256 = hashlib.sha256()
    with open(jar_path, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            sha256.update(chunk)
    actual_hash = sha256.hexdigest()

    with open(checksums_path, "r", encoding="utf-8") as f:
        checksums_content = f.read()

    if actual_hash not in checksums_content:
        print(f"Error: Calculated hash {actual_hash} not found in {checksums_path}!")
        sys.exit(1)

    print(f"  [OK] SHA-256 checksum verified successfully: {actual_hash}")
    print("Verification completed successfully with exit code 0.")

if __name__ == "__main__":
    main()
