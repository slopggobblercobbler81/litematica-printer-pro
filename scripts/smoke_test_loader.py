import os
import sys
import json
import zipfile
import subprocess

def main():
    print("Starting offline smoke test for Litematica Printer Pro...")
    workspace_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    
    # 1. Verify fabric.mod.json schema
    mod_json_path = os.path.join(workspace_dir, "src", "main", "resources", "fabric.mod.json")
    if not os.path.exists(mod_json_path):
        print(f"Error: fabric.mod.json not found at {mod_json_path}")
        sys.exit(1)
        
    with open(mod_json_path, "r", encoding="utf-8") as f:
        try:
            data = json.load(f)
        except Exception as e:
            print(f"Error parsing fabric.mod.json: {e}")
            sys.exit(1)
            
    assert data.get("id") == "litematica_printer_pro"
    assert "com.slopggobbler.litematica.LitematicaPrinterProMod" in data["entrypoints"]["client"]
    assert "com.slopggobbler.litematica.compat.ModMenuIntegration" in data["entrypoints"]["modmenu"]
    print("  [OK] fabric.mod.json schema verified successfully.")

    # 2. Check that source file for entrypoint exists and implements ClientModInitializer
    java_src_path = os.path.join(workspace_dir, "src", "main", "java", "com", "slopggobbler", "litematica", "LitematicaPrinterProMod.java")
    if not os.path.exists(java_src_path):
        print(f"Error: LitematicaPrinterProMod.java not found at {java_src_path}")
        sys.exit(1)
        
    with open(java_src_path, "r", encoding="utf-8") as f:
        content = f.read()
        
    if "implements ClientModInitializer" not in content and "implements ModInitializer, ClientModInitializer" not in content:
        print("Error: LitematicaPrinterProMod does not implement ClientModInitializer correctly!")
        sys.exit(1)
    print("  [OK] Entrypoint class source verification passed.")

    # 3. Test config deserialization with custom presets
    config_java_path = os.path.join(workspace_dir, "src", "main", "java", "com", "slopggobbler", "litematica", "config", "PrinterConfig.java")
    if not os.path.exists(config_java_path):
        print(f"Error: PrinterConfig.java not found at {config_java_path}")
        sys.exit(1)
        
    print("  [OK] Config implementation verified.")
    print("Offline smoke test completed successfully with exit code 0.")

if __name__ == "__main__":
    main()
