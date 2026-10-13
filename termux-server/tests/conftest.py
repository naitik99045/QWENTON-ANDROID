import os
import pytest
from fastapi.testclient import TestClient
from app.main import app
from app import config

@pytest.fixture(scope="session")
def test_client():
    """Provides a session-wide TestClient for API tests."""
    with TestClient(app) as c:
        yield c

@pytest.fixture(autouse=True)
def clean_test_environment(tmp_path, monkeypatch):
    """Isolate test directory and environment variables for deterministic policy testing."""
    test_home = tmp_path / "termux_home"
    test_home.mkdir(parents=True, exist_ok=True)
    monkeypatch.setenv("HOME", str(test_home))
    monkeypatch.setenv("AI_PROVIDER", "mock")
    config.ALLOWED_DIRECTORIES = [str(test_home), config.TERMUX_HOME]
