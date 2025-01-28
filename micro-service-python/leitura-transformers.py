import pytesseract
from PIL import Image
from transformers import pipeline, AutoTokenizer, AutoModelForSequenceClassification
import re
import os

# Configuração do Tesseract OCR
# Certifique-se de que o Tesseract está instalado e configurado no sistema
pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'  # Caminho para o executável do Tesseract no Windows


# Função para extração de texto de imagens
def extract_text_from_image(image_path):
    try:
        image = Image.open(image_path)
        text = pytesseract.image_to_string(image, lang='eng')  # Adicione outros idiomas, se necessário
        return text
    except Exception as e:
        print(f"Erro ao processar a imagem: {e}")
        return ""


# Função para limpeza de texto extraído
def clean_extracted_text(text):
    # Remover espaços extras, caracteres indesejados e normalizar o texto
    cleaned_text = re.sub(r'\s+', ' ', text)
    cleaned_text = re.sub(r'[^\w\s.,!?\-]', '', cleaned_text)
    return cleaned_text.strip()


# Classificação e análise de texto com Transformers
def analyze_text_with_transformers(text, model_name="bert-base-uncased"):
    try:
        # Carregando o modelo e o tokenizer
        tokenizer = AutoTokenizer.from_pretrained(model_name)
        model = AutoModelForSequenceClassification.from_pretrained(model_name)

        # Criação do pipeline para classificação de texto
        classifier = pipeline("text-classification", model=model, tokenizer=tokenizer)

        # Análise do texto
        analysis = classifier(text)
        return analysis
    except Exception as e:
        print(f"Erro durante a análise do texto com Transformers: {e}")
        return []


# Função principal para processamento
def process_image_text(image_path):
    print(f"Processando a imagem: {image_path}")

    # Extração de texto
    extracted_text = extract_text_from_image(image_path)
    print(f"Texto extraído bruto: \n{extracted_text}")

    # Limpeza do texto
    cleaned_text = clean_extracted_text(extracted_text)
    print(f"Texto limpo: \n{cleaned_text}")

    if not cleaned_text:
        print("Nenhum texto significativo encontrado na imagem.")
        return

    # Análise do texto
    print("Analisando o texto com Transformers...")
    analysis = analyze_text_with_transformers(cleaned_text)
    print(f"Resultados da análise: {analysis}")


if __name__ == "__main__":
    # Caminho para a imagem
    image_path = "imagem-nitida.png"  # Substitua pelo caminho da sua imagem

    if os.path.exists(image_path):
        process_image_text(image_path)
    else:
        print(f"O arquivo especificado não foi encontrado: {image_path}")
