from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from typing import List
from .services.ocr_service import extract_text
from .services.llm_service import grade_text
from .schemas import GradeResponse, HealthResponse

router = APIRouter()


@router.get("/health", response_model=HealthResponse)
def health():
    return HealthResponse()


@router.post("/ocr-grade", response_model=GradeResponse)
async def ocr_grade(
        # escolha UMA das linhas abaixo conforme sua preferência:
        file: UploadFile | None = File(None),  # um arquivo
        files: List[UploadFile] | None = File(None),
        # OU vários arquivos (se usar essa, mande pelo Android como "files")

        # novos campos:
        question: str | None = Form(None),  # PERGUNTA (enunciado)
        student_text: str | None = Form(None),  # RESPOSTA digitada pelo aluno
        answer_key: str | None = Form(None),  # GABARITO/RESPOSTAS ESPERADAS
        language: str = Form("por"),
        instructions: str | None = Form(None),
):
    try:
        texts = []

        # OCR de arquivo(s)
        if file is not None:
            t, _ = extract_text(file, language=language)
            if t:
                texts.append(t)
        if files:
            for f in files:
                t, _ = extract_text(f, language=language)
                if t:
                    texts.append(t)

        ocr_text = "\n\n---\n\n".join(texts).strip()

        # Monta prompt com pergunta + resposta do aluno + OCR
        blocks = []
        if question:
            blocks.append(f"Enunciado/Questão:\n{question}")
        if student_text:
            blocks.append(f"Resposta digitada pelo aluno:\n{student_text}")
        if ocr_text:
            blocks.append(f"Texto extraído via OCR (normalizar se necessário):\n{ocr_text}")

        if answer_key:
            blocks.append(f"Gabarito/Respostas esperadas:\n{answer_key}")
        else:
            blocks.append("(Sem gabarito fornecido; estime acertos e informe que é estimativa.)")

        final_prompt = (
                "\n\n".join(blocks)
                + "\n\nTarefa: Avalie e retorne em JSON com {percentual, pontos_fortes, pontos_de_melhoria, observacoes}. "
                  "O campo 'percentual' deve ser apenas um número (0-100)."
        )
        if instructions:
            final_prompt += f"\n\nInstruções adicionais: {instructions}"

        deepseek_resp = grade_text(final_prompt)  # com retry 3x no seu llm_service.py
        return GradeResponse(ocr_text=ocr_text, deepseek_response=deepseek_resp)

    except Exception as e:
        raise HTTPException(status_code=502, detail=f"LLM/OCR error: {e}")
