import asyncio

import pytest
from pydantic import BaseModel

from app.core.config import Settings
from app.services.gemini_client import GeminiClient, LLMError, _describe, _is_transient


def test_missing_gemini_key_fails_without_exposing_configuration():
    settings = Settings(environment="development", auth_mode="firebase", gemini_api_key="")
    with pytest.raises(LLMError, match="GEMINI_API_KEY is not set"):
        GeminiClient(settings)


def test_provider_error_description_never_echoes_secret():
    error = RuntimeError("request failed with key secret-value")
    assert "secret-value" not in _describe(error)


@pytest.mark.parametrize("status", [408, 429, 500, 502, 503, 504])
def test_transient_http_errors_are_retried(status):
    error = type("ProviderError", (Exception,), {"status_code": status})()
    assert _is_transient(error)


def test_timeout_is_transient():
    assert _is_transient(asyncio.TimeoutError())


def test_json_config_error_is_mapped_to_llm_error(monkeypatch):
    """An SDK/schema mismatch must never surface as an API HTTP 500."""
    client = object.__new__(GeminiClient)

    class BrokenConfig:
        def __init__(self, **_):
            raise RuntimeError("provider schema error with private prompt")

    import google.genai.types as types
    monkeypatch.setattr(types, "GenerateContentConfig", BrokenConfig)
    with pytest.raises(LLMError, match="Gemini request failed"):
        asyncio.run(client.generate_json(model="test-model", prompt="private", schema=BaseModel))
