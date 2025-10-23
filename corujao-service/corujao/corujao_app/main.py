from fastapi import FastAPI
from corujao_app.api import router as api_router

app = FastAPI(title="OCR + DeepSeek Service", version="1.0.0")
app.include_router(api_router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("corujao_app.main:app", host="0.0.0.0", port=8001, reload=True)
