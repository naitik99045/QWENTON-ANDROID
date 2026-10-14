import pytest
from pathlib import Path
from app.tools.coding import workspace_tree, write_code_file, read_code_file, patch_code_file
from app.tools.pentest import run_sast_scan, run_port_audit
from app.tools.adb_bridge import (
    build_adb_device_status_args,
    build_adb_list_packages_args,
    build_adb_tap_args,
    build_adb_launch_app_args
)

def test_ai_config_api(client):
    # Test GET /config/ai-provider
    get_resp = client.get("/config/ai-provider")
    assert get_resp.status_code == 200
    data = get_resp.json()
    assert data["status"] == "ok"
    assert "provider" in data
    assert "model" in data

    # Test POST /config/ai-provider with custom provider + base_url + api_key
    custom_resp = client.post("/config/ai-provider", json={
        "provider": "custom",
        "api_key": "sk-or-v1-abcdefghijklmnopqrstuvwxyz123456",
        "model": "deepseek/deepseek-chat",
        "base_url": "https://openrouter.ai/api/v1"
    })
    assert custom_resp.status_code == 200
    custom_data = custom_resp.json()
    assert custom_data["provider"] == "custom"
    assert custom_data["model"] == "deepseek/deepseek-chat"
    assert custom_data["base_url"] == "https://openrouter.ai/api/v1"
    assert custom_data["api_key_masked"].startswith("sk-o")

    # Verify GET returns the custom base_url
    get_custom = client.get("/config/ai-provider")
    assert get_custom.status_code == 200
    assert get_custom.json()["base_url"] == "https://openrouter.ai/api/v1"
    assert get_custom.json()["provider"] == "custom"

def test_coding_tools_lifecycle(tmp_path):
    base_dir = str(tmp_path)
    
    # 1. Write file
    test_content = "def hello():\n    print('Hello World')\n"
    res = write_code_file("hello.py", test_content, base_dir=base_dir)
    assert "Successfully wrote" in res

    # 2. Read file
    read_res = read_code_file("hello.py", base_dir=base_dir, start_line=1, end_line=10)
    assert "Hello World" in read_res
    assert "hello.py" in read_res

    # 3. Surgical Patch
    patch_res = patch_code_file(
        "hello.py",
        search_chunk="print('Hello World')",
        replace_chunk="print('Hello Qwenton')",
        base_dir=base_dir
    )
    assert "Surgical patch applied" in patch_res

    # 4. Verify patch
    updated_read = read_code_file("hello.py", base_dir=base_dir)
    assert "Hello Qwenton" in updated_read
    assert "Hello World" not in updated_read

    # 5. Tree check
    tree_res = workspace_tree(".", base_dir=base_dir)
    assert "hello.py" in tree_res

def test_pentest_sast_scanner(tmp_path):
    base_dir = str(tmp_path)
    # Write a test vulnerable script with a dummy key and dangerous eval
    vuln_code = (
        "# Test script\n"
        "GEMINI_KEY = 'AIzaSyFakeKey1234567890abcdefghijkl'\n"
        "eval('1 + 1')\n"
    )
    write_code_file("vuln_test.py", vuln_code, base_dir=base_dir)

    report = run_sast_scan(".", base_dir=base_dir)
    assert "Qwenton Defensive Security Audit Report" in report
    assert "Google API / Firebase Key" in report
    assert "Dangerous Code Evaluation (eval)" in report

def test_adb_bridge_vectors():
    status_args = build_adb_device_status_args()
    assert status_args == ["adb", "devices", "-l"]

    pkg_args = build_adb_list_packages_args(third_party_only=True)
    assert pkg_args == ["adb", "shell", "pm", "list", "packages", "-3"]

    tap_args = build_adb_tap_args(500, 1000)
    assert tap_args == ["adb", "shell", "input", "tap", "500", "1000"]

    launch_args = build_adb_launch_app_args("com.android.settings")
    assert "com.android.settings" in launch_args

@pytest.mark.asyncio
async def test_ai_app_and_call_intents():
    from app.ai_client import AIClient
    client = AIClient(provider="mock")

    # 1. Open app intent
    open_resp = await client.generate_response("r1", "c1", "open spotify")
    assert open_resp.type == "tool_request"
    assert open_resp.tool == "open_app"
    assert open_resp.arguments.get("app_name") == "spotify"

    # 2. Call contact intent
    call_resp = await client.generate_response("r2", "c1", "call mom")
    assert call_resp.type == "tool_request"
    assert call_resp.tool == "call_contact"
    assert call_resp.arguments.get("contact_name") == "mom"

    # 3. Pairing code intent
    pair_resp = await client.generate_response("r3", "c1", "get wireless debugging pairing code")
    assert pair_resp.type == "tool_request"
    assert pair_resp.tool == "pair_wireless_debugging"

