from flask import Flask, request, jsonify
from PIL import Image
import pytesseract
from transformers import pipeline, AutoTokenizer, AutoModelForSequenceClassification
import re
import base64
import io

# Configuração do Tesseract OCR
pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'  # Altere se necessário

# Inicialização do Flask
app = Flask(__name__)

# Função para extração de texto de imagens
def extract_text_from_image(image):
    try:
        text = pytesseract.image_to_string(image, lang='eng')  # Adicione outros idiomas se necessário
        return text
    except Exception as e:
        print(f"Erro ao processar a imagem: {e}")
        return ""

# Função para limpeza de texto extraído
def clean_extracted_text(text):
    cleaned_text = re.sub(r'\s+', ' ', text)  # Remove espaços extras
    cleaned_text = re.sub(r'[^\w\s.,!?-]', '', cleaned_text)  # Remove caracteres indesejados
    return cleaned_text.strip()

# Classificação e análise de texto com Transformers
def analyze_text_with_transformers(text, model_name="bert-base-uncased"):
    try:
        # Carrega o modelo e o tokenizer
        tokenizer = AutoTokenizer.from_pretrained(model_name)
        model = AutoModelForSequenceClassification.from_pretrained(model_name)

        # Cria o pipeline para classificação de texto
        classifier = pipeline("text-classification", model=model, tokenizer=tokenizer)

        # Analisa o texto
        analysis = classifier(text)
        return analysis
    except Exception as e:
        print(f"Erro durante a análise do texto com Transformers: {e}")
        return []

# Endpoint para processamento de imagem
@app.route('/process-image', methods=['POST'])
def process_image():
    try:
        # Recebe a imagem em Base64 do corpo da requisição
        data = request.json
        image_base64 = data.get("image")
        if not image_base64:
            return jsonify({"error": "Imagem não fornecida"}), 400

        # Decodifica a imagem Base64
        image_data = base64.b64decode(image_base64)
        image = Image.open(io.BytesIO(image_data))

        # Processa a imagem para extração de texto
        extracted_text = extract_text_from_image(image)
        cleaned_text = clean_extracted_text(extracted_text)

        if not cleaned_text:
            return jsonify({"error": "Nenhum texto significativo encontrado na imagem"}), 400

        # Analisa o texto com Transformers
        analysis = analyze_text_with_transformers(cleaned_text)

        # Retorna o texto e a análise
        return jsonify({
            "extracted_text": cleaned_text,
            "analysis": analysis
        })

    except Exception as e:
        print(f"Erro no processamento: {e}")
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=5000)
