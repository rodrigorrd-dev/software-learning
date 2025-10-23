import time
import requests
from ..settings import settings

OPENROUTER_ENDPOINT = ""

def grade_text(prompt: str) -> str:
    headers = {
        "Authorization": f"Bearer {settings.OPENROUTER_API_KEY}",
        "Content-Type": "application/json",
        "HTTP-Referer": "http://localhost:8001",
        "X-Title": "OCR Grade Gateway",
    }
    body = {
        "model": settings.OPENROUTER_MODEL,
        "messages": [
            {
                "role": "system",
                "content": "Você é um avaliador de provas que calcula percentual de acertos e explica brevemente."
            },
            {"role": "user", "content": prompt},
        ],
        "temperature": 0.2,
    }

    max_retries = 3
    delay = 2.0  # segundos (backoff exponencial: 2s -> 4s -> 8s)

    for attempt in range(1, max_retries + 1):
        r = requests.post(OPENROUTER_ENDPOINT, headers=headers, json=body, timeout=90)

        if r.status_code == 200:
            return r.json()["choices"][0]["message"]["content"]

        if r.status_code == 429 and attempt < max_retries:
            # Rate limit: espera e tenta de novo
            print(f"[WARN] OpenRouter 429 (tentativa {attempt}/{max_retries-1}). "
                  f"Re-tentando em {delay:.0f}s…")
            time.sleep(delay)
            delay *= 2
            continue

        # Outros erros (401/403/5xx) ou 429 na última tentativa
        raise RuntimeError(f"OpenRouter {r.status_code}: {r.text}")

    # Se chegou aqui, foi 429 nas 3 tentativas
    raise RuntimeError("OpenRouter 429 persistente após 3 tentativas.")