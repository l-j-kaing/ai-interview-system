
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from app.core.vector_store import vector_store

print("Collection:", vector_store.collection_name)
print("字段:", vector_store.col.schema.fields)
print("向量字段:", vector_store.vector_fields)
