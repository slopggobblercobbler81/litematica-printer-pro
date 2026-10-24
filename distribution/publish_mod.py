#!/usr/bin/env python3
"""
Automated publishing script for LiteBuilder / Litematica Printer Pro.
Uploads release jars directly to Modrinth and CurseForge using their REST APIs.
"""

import os
import sys
import json
import argparse
import urllib.request
import urllib.error
import uuid

VERSION = "1.0.0"
CHANGELOG_PATH = "distribution/RELEASE_NOTES.md"
ICON_PATH = "src/main/resources/assets/litebuilder/icon.png"

def get_jar_path():
    candidates = [
        "build/libs/litebuilder-1.0.0.jar",
        "build/libs/litebuilder-26.3-1.0.0.jar",
        "build/libs/litematica-printer-pro-26.3-1.0.0.jar",
        "build/libs/litematica-printer-pro-1.0.0.jar"
    ]
    for c in candidates:
        if os.path.exists(c):
            return c
    return candidates[0]

def get_changelog():
    if os.path.exists(CHANGELOG_PATH):
        with open(CHANGELOG_PATH, "r", encoding="utf-8") as f:
            return f.read()
    return f"LiteBuilder v{VERSION} Release"

def update_modrinth_metadata(token, project_id):
    desc_path = "distribution/MODRINTH_DESCRIPTION.md"
    if not os.path.exists(desc_path):
        return

    print(f"[Modrinth] Updating project '{project_id}' description and links...")
    with open(desc_path, "r", encoding="utf-8") as f:
        body_text = f.read()

    url = f"https://api.modrinth.com/v2/project/{project_id}"
    patch_data = {
        "body": body_text,
        "issues_url": "https://github.com/slopggobblercobbler81/litematica-printer-pro/issues",
        "source_url": "https://github.com/slopggobblercobbler81/litematica-printer-pro"
    }

    req = urllib.request.Request(url, data=json.dumps(patch_data).encode("utf-8"), method="PATCH")
    req.add_header("Authorization", token)
    req.add_header("Content-Type", "application/json")
    req.add_header("User-Agent", "slopggobbler/litebuilder")

    try:
        with urllib.request.urlopen(req) as resp:
            print(f"[Modrinth] Project metadata updated! Status: {resp.status}")
    except Exception as e:
        print(f"[Modrinth Patch Warning] {e}")

    if os.path.exists(ICON_PATH):
        print(f"[Modrinth] Updating project icon...")
        icon_url = f"https://api.modrinth.com/v2/project/{project_id}/icon?ext=png"
        with open(ICON_PATH, "rb") as f:
            icon_bytes = f.read()
        icon_req = urllib.request.Request(icon_url, data=icon_bytes, method="PATCH")
        icon_req.add_header("Authorization", token)
        icon_req.add_header("Content-Type", "image/png")
        icon_req.add_header("User-Agent", "slopggobbler/litebuilder")
        try:
            with urllib.request.urlopen(icon_req) as resp:
                print(f"[Modrinth] Project icon updated!")
        except Exception as e:
            print(f"[Modrinth Icon Warning] {e}")

def publish_to_modrinth(token, project_id):
    jar_path = get_jar_path()
    print(f"[Modrinth] Publishing v{VERSION} to project '{project_id}' using {jar_path}...")
    if not os.path.exists(jar_path):
        print(f"[Error] Jar file not found at {jar_path}")
        return False

    update_modrinth_metadata(token, project_id)

    url = "https://api.modrinth.com/v2/version"
    metadata = {
        "name": f"LiteBuilder {VERSION} (Minecraft 26.3 & 1.21.4)",
        "version_number": VERSION,
        "changelog": get_changelog(),
        "dependencies": [
            {"project_id": "P7dR8mSH", "dependency_type": "required"},  # fabric-api
            {"project_id": "GcWjdA9I", "dependency_type": "required"},  # malilib
            {"project_id": "bEpr0Arc", "dependency_type": "required"}   # litematica
        ],
        "game_versions": ["1.21.4", "26.3"],
        "version_type": "release",
        "loaders": ["fabric", "quilt"],
        "featured": True,
        "project_id": project_id,
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

    filename = os.path.basename(jar_path)
    body.extend(f"--{boundary}\r\n".encode("utf-8"))
    body.extend(f'Content-Disposition: form-data; name="primary_file"; filename="{filename}"\r\n'.encode("utf-8"))
    body.extend(b"Content-Type: application/java-archive\r\n\r\n")
    with open(jar_path, "rb") as f:
        body.extend(f.read())
    body.extend(b"\r\n")
    body.extend(f"--{boundary}--\r\n".encode("utf-8"))

    req = urllib.request.Request(url, data=body, method="POST")
    req.add_header("Authorization", token)
    req.add_header("Content-Type", f"multipart/form-data; boundary={boundary}")
    req.add_header("User-Agent", f"slopggobbler/litebuilder/{VERSION}")

    try:
        with urllib.request.urlopen(req) as resp:
            resp_data = json.loads(resp.read().decode("utf-8"))
            print(f"[Modrinth] Success! Published version id: {resp_data.get('id')}")
            return True
    except urllib.error.HTTPError as e:
        print(f"[Modrinth Error] HTTP {e.code}: {e.read().decode('utf-8')}")
        return False
    except Exception as e:
        print(f"[Modrinth Error] {e}")
        return False

def publish_to_curseforge(token, project_id):
    jar_path = get_jar_path()
    print(f"[CurseForge] Publishing v{VERSION} to project '{project_id}' using {jar_path}...")
    if not os.path.exists(jar_path):
        print(f"[Error] Jar file not found at {jar_path}")
        return False

    url = f"https://minecraft.curseforge.com/api/projects/{project_id}/upload-file"
    metadata = {
        "changelog": get_changelog(),
        "changelogType": "markdown",
        "displayName": f"LiteBuilder v{VERSION} (Minecraft 26.3 & 1.21.4)",
        "gameVersions": [9638, 7499, 9153, 12281, 17045],  # Client (9638), Fabric, Quilt, 1.21.4, 26.3
        "releaseType": "release"
    }

    boundary = f"----WebKitFormBoundary{uuid.uuid4().hex}"
    body = bytearray()

    body.extend(f"--{boundary}\r\n".encode("utf-8"))
    body.extend(b'Content-Disposition: form-data; name="metadata"\r\n')
    body.extend(b'Content-Type: application/json\r\n\r\n')
    body.extend(json.dumps(metadata).encode("utf-8"))
    body.extend(b"\r\n")

    filename = os.path.basename(jar_path)
    body.extend(f"--{boundary}\r\n".encode("utf-8"))
    body.extend(f'Content-Disposition: form-data; name="file"; filename="{filename}"\r\n'.encode("utf-8"))
    body.extend(b"Content-Type: application/java-archive\r\n\r\n")
    with open(jar_path, "rb") as f:
        body.extend(f.read())
    body.extend(b"\r\n")
    body.extend(f"--{boundary}--\r\n".encode("utf-8"))

    req = urllib.request.Request(url, data=body, method="POST")
    req.add_header("X-Api-Token", token)
    req.add_header("Content-Type", f"multipart/form-data; boundary={boundary}")
    req.add_header("User-Agent", f"slopggobbler/litebuilder/{VERSION}")

    try:
        with urllib.request.urlopen(req) as resp:
            resp_data = json.loads(resp.read().decode("utf-8"))
            print(f"[CurseForge] Success! Uploaded file id: {resp_data.get('id')}")
            return True
    except urllib.error.HTTPError as e:
        print(f"[CurseForge Error] HTTP {e.code}: {e.read().decode('utf-8')}")
        return False
    except Exception as e:
        print(f"[CurseForge Error] {e}")
        return False

def main():
    root = os.path.abspath("D:/GEMINI AGY/workspaces/litematica-printer-pro")
    if os.path.exists(root):
        os.chdir(root)

    parser = argparse.ArgumentParser(description="Publish LiteBuilder to Modrinth & CurseForge")
    parser.add_argument("--modrinth-token", default=os.getenv("MODRINTH_TOKEN"))
    parser.add_argument("--modrinth-id", default=os.getenv("MODRINTH_PROJECT_ID", "QOOMekOz"))
    parser.add_argument("--curseforge-token", default=os.getenv("CURSEFORGE_TOKEN"))
    parser.add_argument("--curseforge-id", default=os.getenv("CURSEFORGE_PROJECT_ID", "1758829"))
    parser.add_argument("--all", action="store_true")

    args = parser.parse_args()

    executed = False
    if args.modrinth_token:
        executed = True
        publish_to_modrinth(args.modrinth_token, args.modrinth_id)

    if args.curseforge_token and args.curseforge_id:
        executed = True
        publish_to_curseforge(args.curseforge_token, args.curseforge_id)

    if not executed:
        print("\nNo valid tokens found. Please provide MODRINTH_TOKEN and CURSEFORGE_TOKEN.")

if __name__ == "__main__":
    main()
