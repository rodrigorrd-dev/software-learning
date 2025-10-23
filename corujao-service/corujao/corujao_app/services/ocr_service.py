import requests
from werkzeug.utils import secure_filename
from ..settings import settings

OCR_ENDPOINT = ""

def extract_text(file_storage, language: str = "por"):
    files = {
        "file": (secure_filename(file_storage.filename), file_storage.file, file_storage.content_type or "application/octet-stream")
    }
    data = {"language": language, "isTable": True, "OCREngine": 2}
    headers = {"apikey": settings.OCR_SPACE_API_KEY}
    resp = requests.post(OCR_ENDPOINT, files=files, data=data, headers=headers, timeout=60)
    resp.raise_for_status()
    payload = resp.json()
    texts = [pr.get("ParsedText", "") for pr in payload.get("ParsedResults", [])]
    return "\n".join(texts).strip(), payload
