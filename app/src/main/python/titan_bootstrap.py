"""TITAN Android bootstrap — run full engine on 127.0.0.1:5000."""
from __future__ import annotations

import importlib.util
import os
import sys
import threading
import time
from pathlib import Path


def _find_engine() -> Path:
    here = Path(__file__).resolve().parent
    for name in ("TITAN_PRO_V75_PWA.py", "titan_engine.py"):
        p = here / name
        if p.is_file() and p.stat().st_size > 100_000:
            return p
    raise FileNotFoundError(
        "TITAN_PRO_V75_PWA.py not found in app python folder. "
        "Upload your full script to app/src/main/python/ on GitHub."
    )


def start_titan(files_dir: str, port: int = 5000) -> None:
    os.environ["TITAN_HOME"] = str(Path(files_dir) / "LLL")
    os.environ["TITAN_OPEN_BROWSER"] = "0"
    os.environ.setdefault("TMPDIR", str(Path(files_dir) / "tmp"))
    Path(os.environ["TITAN_HOME"]).mkdir(parents=True, exist_ok=True)
    Path(os.environ["TMPDIR"]).mkdir(parents=True, exist_ok=True)

    engine = _find_engine()
    # Copy into writable files dir and patch Android storage hard-lock
    dest_dir = Path(files_dir) / "engine"
    dest_dir.mkdir(parents=True, exist_ok=True)
    dest = dest_dir / "TITAN_PRO_V75_PWA.py"
    text = engine.read_text(encoding="utf-8")
    text = text.replace("if android_storage_present:", "if False and android_storage_present:", 1)
    text = text.replace('if __name__ == "__main__":', 'if False and __name__ == "__main__":')
    dest.write_text(text, encoding="utf-8")

    sys.path.insert(0, str(dest_dir))
    spec = importlib.util.spec_from_file_location("titan_engine", str(dest))
    mod = importlib.util.module_from_spec(spec)
    sys.modules["titan_engine"] = mod
    assert spec.loader is not None
    spec.loader.exec_module(mod)

    if hasattr(mod, "run_titan"):
        def _go():
            try:
                mod.run_titan(open_browser=False)
            except TypeError:
                mod.run_titan()
        threading.Thread(target=_go, name="titan-flask", daemon=True).start()
        return

    app = getattr(mod, "app", None)
    if app is not None:
        def _go():
            app.run(host="127.0.0.1", port=port, threaded=True, use_reloader=False)
        threading.Thread(target=_go, name="titan-flask", daemon=True).start()
        return
    raise RuntimeError("TITAN entry point not found")


def wait_ready(url: str = "http://127.0.0.1:5000/health", timeout: float = 120.0) -> bool:
    import urllib.request

    deadline = time.time() + timeout
    while time.time() < deadline:
        try:
            with urllib.request.urlopen(url, timeout=2) as r:
                if r.status == 200:
                    return True
        except Exception:
            time.sleep(0.8)
    return False
