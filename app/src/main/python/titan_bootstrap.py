"""TITAN Android bootstrap — extract embedded engine and run Flask on 127.0.0.1:5000."""
from __future__ import annotations

import base64
import gzip
import os
import sys
import threading
import time
from pathlib import Path


def _extract_engine(dest: Path) -> Path:
    dest.mkdir(parents=True, exist_ok=True)
    out = dest / "TITAN_PRO_V75_PWA.py"
    if out.is_file() and out.stat().st_size > 1_000_000:
        return out
    here = Path(__file__).resolve().parent
    chunks = []
    for i in range(16):
        p = here / f"titan_part_{i:02d}.b64"
        if not p.is_file():
            break
        chunks.append(base64.b64decode(p.read_text(encoding="ascii").strip()))
    if not chunks:
        raise FileNotFoundError("TITAN engine parts missing")
    raw = gzip.decompress(b"".join(chunks))
    text = raw.decode("utf-8")
    # Allow app-private TITAN_HOME on Android when shared LLL is not writable
    text = text.replace(
        "if android_storage_present:",
        "if False and android_storage_present:",
        1,
    )
    out.write_text(text, encoding="utf-8")
    return out


def start_titan(files_dir: str, port: int = 5000) -> None:
    os.environ["TITAN_HOME"] = str(Path(files_dir) / "LLL")
    os.environ["TITAN_OPEN_BROWSER"] = "0"
    os.environ.setdefault("TMPDIR", str(Path(files_dir) / "tmp"))
    Path(os.environ["TITAN_HOME"]).mkdir(parents=True, exist_ok=True)
    Path(os.environ["TMPDIR"]).mkdir(parents=True, exist_ok=True)
    engine = _extract_engine(Path(files_dir) / "engine")
    sys.path.insert(0, str(engine.parent))
    import importlib.util

    code = engine.read_text(encoding="utf-8")
    guarded = code.replace('if __name__ == "__main__":', 'if False and __name__ == "__main__":')
    tmp = engine.parent / "_titan_load.py"
    tmp.write_text(guarded, encoding="utf-8")
    spec = importlib.util.spec_from_file_location("titan_engine", str(tmp))
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
    raise RuntimeError("TITAN entry point not found (run_titan/app)")


def wait_ready(url: str = "http://127.0.0.1:5000/health", timeout: float = 90.0) -> bool:
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
