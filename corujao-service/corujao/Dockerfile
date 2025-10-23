FROM python:3.10-slim

WORKDIR /app

# Evita bytecode e output bufferizado
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Exponha a porta do serviço
EXPOSE 8001

# Leia configurações do .env se presente no container
# (pydantic-settings procura .env na raiz por padrão)
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]
