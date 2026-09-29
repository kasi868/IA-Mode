import pytest

from app.services import prompts


def test_all_required_prompt_assets_are_present():
    prompts.validate_prompt_assets()


def test_missing_prompt_assets_fail_deployment_startup(monkeypatch, tmp_path):
    monkeypatch.setattr(prompts, "PROMPT_DIR", tmp_path)
    with pytest.raises(RuntimeError, match="Required prompt assets are missing"):
        prompts.validate_prompt_assets()
