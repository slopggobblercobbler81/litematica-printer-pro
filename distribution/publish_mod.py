#!/usr/bin/env python3
"""
Automated publishing script for Litematica Printer Pro.
Uploads release jars directly to Modrinth and CurseForge using their REST APIs.

Usage:
  python publish_mod.py --modrinth-token <TOKEN> --modrinth-id <ID_OR_SLUG>
  python publish_mod.py --curseforge-token <TOKEN> --curseforge-id <PROJECT_ID>
  python publish_mod.py --all  (reads MODRINTH_TOKEN, MODRINTH_PROJECT_ID, CURSEFORGE_TOKEN, CURSEFORGE_PROJECT_ID from env)
"""

import os
import sys
import json
import argparse
import urllib.request
import urllib.parse
import mimetypes
import uuid

JAR_PATH = "build/libs/litematica-printer-pro-26.3-1.0.0.jar"
CHANGELOG_PATH = "distribution/RELEASE_NOTES.md"
VERSION = "1.0.0"
GAME_VERSIONS = ["26.3", "1.21.4"]
LOADERS = ["fabric", "quilt"]

def get_changelog():
    if os.path.exists(CHANGELOG_PATH):
        with open(CHANGELOG_PATH, "r", encoding="utf-8") as f:
            return f.read()
    return f"Litematica Printer Pro v{VERSION} Release"

def publish_to_modrinth(token, project_id):
    print(f"[Modrinth] Publishing v{VERSION} to project '{project_id}'...")
    if not os.path.exists(JAR_PATH):
        print(f"[Error] Jar file not found at {JAR_PATH}")
        return False

    url = "https://api.modrinth.com/v2/version"
    metadata = {
        "name": f"Litematica Printer Pro v{VERSION} (Minecraft 26.3 & 1.21.4)",
        "version_number": VERSION,
        "changelog": get_changelog(),
        "dependencies": [
            {"project_id": "P7dR8mSH", "dependency_type": "required"},  # fabric-api
            {"project_id": "gvQqBUqZ", "dependency_type": "required"},  # malilib
            {"project_id": "wbTI9hoV", "dependency_type": "required"}   # litematica
        ],
        "game_versions": GAME_VERSIONS,
        "version_type": "release",
        "loaders": LOADERS,
        "featured": True,
        "project_id": project_id,
        "file_parts": ["primary_file"],
        "primary_file": "primary_file"
    }

    boundary = f"----WebKitFormBoundary{uuid.uuid4().hex}"
    body = bytearray()

    # Add metadata part
    body.extend(f"--{boundary}\r\n".encode("utf-8"))
    body.extend(b'Content-Disposition: form-data; name="data"\r\n')
    body.extend(b'Content-Type: application/json\r\n\r\n')
    body.extend(json.dumps(metadata).encode("utf-8"))
    body.extend(b"\r\n")

    # Add file part
    filename = os.path.basename(JAR_PATH)
    body.extend(f"--{boundary}\r\n".encode("utf-8"))
    body.extend(f'Content-Disposition: form-data; name="primary_file"; filename="{filename}"\r\n'.encode("utf-8"))
    body.extend(b"Content-Type: application/java-archive\r\n\r\n")
    with open(JAR_PATH, "rb") as f:
        body.extend(f.read())
    body.extend(b"\r\n")
    body.extend(f"--{boundary}--\r\n".encode("utf-8"))

    req = urllib.request.Request(url, data=body, method="POST")
    req.add_header("Authorization", token)
    req.add_header("Content-Type", f"multipart/form-data; boundary={boundary}")
    req.add_header("User-Agent", f"slopggobbler/litematica-printer-pro/{VERSION}")

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
    print(f"[CurseForge] Publishing v{VERSION} to project '{project_id}'...")
    if not os.path.exists(JAR_PATH):
        print(f"[Error] Jar file not found at {JAR_PATH}")
        return False

    url = f"https://minecraft.curseforge.com/api/projects/{project_id}/upload-file"
    metadata = {
        "changelog": get_changelog(),
        "changelogType": "markdown",
        "displayName": f"Litematica Printer Pro v{VERSION} (26.3 & 1.21.4)",
        "parentFileID": None,
        "releaseType": "release"
    }

    boundary = f"----WebKitFormBoundary{uuid.uuid4().hex}"
    body = bytearray()

    # Add metadata part
    body.extend(f"--{boundary}\r\n".encode("utf-8"))
    body.extend(b'Content-Disposition: form-data; name="metadata"\r\n')
    body.extend(b'Content-Type: application/json\r\n\r\n')
    body.extend(json.dumps(metadata).encode("utf-8"))
    body.extend(b"\r\n")

    # Add file part
    filename = os.path.basename(JAR_PATH)
    body.extend(f"--{boundary}\r\n".encode("utf-8"))
    body.extend(f'Content-Disposition: form-data; name="file"; filename="{filename}"\r\n'.encode("utf-8"))
    body.extend(b"Content-Type: application/java-archive\r\n\r\n")
    with open(JAR_PATH, "rb") as f:
        body.extend(f.read())
    body.extend(b"\r\n")
    body.extend(f"--{boundary}--\r\n".encode("utf-8"))

    req = urllib.request.Request(url, data=body, method="POST")
    req.add_header("X-Api-Token", token)
    req.add_header("Content-Type", f"multipart/form-data; boundary={boundary}")

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
    parser = argparse.ArgumentParser(description="Publish Litematica Printer Pro to Modrinth & CurseForge")
    parser.add_argument("--modrinth-token", default=os.getenv("MODRINTH_TOKEN"))
    parser.add_argument("--modrinth-id", default=os.getenv("MODRINTH_PROJECT_ID", "litematica-printer-pro"))
    parser.add_argument("--curseforge-token", default=os.getenv("CURSEFORGE_TOKEN"))
    parser.add_argument("--curseforge-id", default=os.getenv("CURSEFORGE_PROJECT_ID"))
    parser.add_argument("--all", action="store_true")

    args = parser.parse_args()

    executed = False
    if args.modrinth_token:
        executed = True
        publish_to_modrinth(args.modrinth_token, args.modrinth_id)
    else:
        print("[Modrinth] No token found in --modrinth-token or MODRINTH_TOKEN.")

    if args.curseforge_token and args.curseforge_id:
        executed = True
        publish_to_curseforge(args.curseforge_token, args.curseforge_id)
    else:
        print("[CurseForge] No token/id found in --curseforge-token/CURSEFORGE_TOKEN or --curseforge-id/CURSEFORGE_PROJECT_ID.")

    if not executed:
        print("\nReady to publish! Provide tokens via environment variables or CLI flags:")
        print("  $env:MODRINTH_TOKEN=\"...\"")
        print("  $env:CURSEFORGE_TOKEN=\"...\"")
        print("  python distribution/publish_mod.py --all")

if __name__ == "__main__":
    main()
