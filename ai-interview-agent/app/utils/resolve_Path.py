from pathlib import Path
from app.core.config import settings
ROOT_DIR = settings.dir_root
def resolve_path(storage_path):
    if not storage_path or not str(storage_path).strip():
        return None
    root_path = Path(ROOT_DIR).resolve()
    relative_path = Path(storage_path)
    if relative_path.is_absolute():
        return None
    target_path = (root_path / relative_path).resolve()
    try:
        target_path.relative_to(root_path)
    except ValueError:
        return None
    return target_path