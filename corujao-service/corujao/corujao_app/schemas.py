from pydantic import BaseModel, Field

class GradeResponse(BaseModel):
    ok: bool = True
    ocr_text: str
    deepseek_response: str

class HealthResponse(BaseModel):
    status: str = Field("ok", description="Service health")
