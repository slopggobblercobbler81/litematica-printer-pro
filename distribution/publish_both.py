import os
import sys
import json
import uuid
import urllib.request
import urllib.error

MODRINTH_TOKEN = os.getenv("MODRINTH_TOKEN", "")
CURSEFORGE_TOKEN = os.getenv("CURSEFORGE_TOKEN", "")
CURSEFORGE_PROJECT_ID = os.getenv("CURSEFORGE_PROJECT_ID", "1758829")
MODRINTH_PROJECT_ID = os.getenv("MODRINTH_PROJECT_ID", "QOOMekOz")

JAR_PATH = "build/libs/litebuilder-1.0.0.jar"
ICON_PATH = "src/main/resources/assets/litebuilder/icon.png"

def read_file(path):
    with open(path, "r", encoding="utf-8") as f:
        return f.read()

def update_modrinth_project_body_and_icon():
    print(f"[Modrinth] Updating project {MODRINTH_PROJECT_ID} description and metadata...")
    url = f"https://api.modrinth.com/v2/project/{MODRINTH_PROJECT_ID}"
    body_text = read_file("distribution/MODRINTH_DESCRIPTION.md")
    
    patch_data = {
        "body": body_text,
        "issues_url": "https://github.com/slopggobblercobbler81/litematica-printer-pro/issues",
        "source_url": "https://github.com/slopggobblercobbler81/litematica-printer-pro"
    }

    req = urllib.request.Request(url, data=json.dumps(patch_data).encode("utf-8"), method="PATCH")
    req.add_header("Authorization", MODRINTH_TOKEN)
    req.add_header("Content-Type", "application/json")
    req.add_header("User-Agent", "slopggobbler/litebuilder")

    try:
        with urllib.request.urlopen(req) as resp:
            print(f"[Modrinth] Project metadata updated! Status: {resp.status}")
    except Exception as e:
        print(f"[Modrinth Patch Error] {e}")

    # Upload icon
    if os.path.exists(ICON_PATH):
        print(f"[Modrinth] Uploading icon for project...")
        icon_url = f"https://api.modrinth.com/v2/project/{MODRINTH_PROJECT_ID}/icon?ext=png"
        with open(ICON_PATH, "rb") as f:
            icon_bytes = f.read()
        icon_req = urllib.request.Request(icon_url, data=icon_bytes, method="PATCH")
        icon_req.add_header("Authorization", MODRINTH_TOKEN)
        icon_req.add_header("Content-Type", "image/png")
        icon_req.add_header("User-Agent", "slopggobbler/litebuilder")
        try:
            with urllib.request.urlopen(icon_req) as resp:
                print(f"[Modrinth] Icon updated successfully!")
        except Exception as e:
            print(f"[Modrinth Icon Error] {e}")

def upload_modrinth_version():
    print(f"[Modrinth] Uploading version 1.0.0...")
    url = "https://api.modrinth.com/v2/version"
    changelog = read_file("distribution/RELEASE_NOTES.md")

    game_versions = [
        "1.20.1", "1.20.2", "1.20.4", "1.20.6",
        "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4",
        "26.3"
    ]

    metadata = {
        "name": "LiteBuilder 1.0.0 (Minecraft 1.20.1 - 26.3)",
        "version_number": "1.0.0",
        "changelog": changelog,
        "dependencies": [
            {"project_id": "P7dR8mSH", "dependency_type": "required"},  # fabric-api
            {"project_id": "GcWjdA9I", "dependency_type": "required"},  # malilib
            {"project_id": "bEpr0Arc", "dependency_type": "required"}   # litematica
        ],
        "game_versions": game_versions,
        "version_type": "release",
        "loaders": ["fabric", "quilt"],
        "featured": True,
        "project_id": MODRINTH_PROJECT_ID,
        "file_parts": ["primary_file"],
        "primary_file": "primary_file"
    }

    boundary = f"----WebKitFormBoundary{uuid.uuid4().hex}"
    body = bytearray()

    body.extend(f"--{boundary}\r\n".encode("utf-8"))
    body.extend(b'Content-Disposition: form-data; name="data"\r\n')
    body.extend(b'Content-Type: application/json\r\n\r\n')
    body.extend(json.dumps(metadata).encode("utf-8"))
    body.extend(b"\r\n")

    filename = os.path.basename(JAR_PATH)
    body.extend(f"--{boundary}\r\n".encode("utf-8"))
    body.extend(f'Content-Disposition: form-data; name="primary_file"; filename="{filename}"\r\n'.encode("utf-8"))
    body.extend(b"Content-Type: application/java-archive\r\n\r\n")
    with open(JAR_PATH, "rb") as f:
        body.extend(f.read())
    body.extend(b"\r\n")
    body.extend(f"--{boundary}--\r\n".encode("utf-8"))

    req = urllib.request.Request(url, data=body, method="POST")
    req.add_header("Authorization", MODRINTH_TOKEN)
    req.add_header("Content-Type", f"multipart/form-data; boundary={boundary}")
    req.add_header("User-Agent", "slopggobbler/litebuilder")

    try:
        with urllib.request.urlopen(req) as resp:
            data = json.loads(resp.read().decode("utf-8"))
            print(f"[Modrinth] Version uploaded successfully! Version ID: {data.get('id')}")
            return True
    except urllib.error.HTTPError as e:
        print(f"[Modrinth Version Error] HTTP {e.code}: {e.read().decode('utf-8')}")
        return False
    except Exception as e:
        print(f"[Modrinth Version Error] {e}")
        return False

def main():
    root = os.path.abspath("D:/GEMINI AGY/workspaces/litematica-printer-pro")
    os.chdir(root)

    print("=== Finalizing Modrinth Deployment ===")
    update_modrinth_project_body_and_icon()
    upload_modrinth_version()

if __name__ == "__main__":
    main()
