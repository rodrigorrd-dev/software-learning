import cv2
import pytesseract
import numpy as np
from imutils import rotate_bound

# Configurar o caminho do executável do Tesseract (somente no Windows)
pytesseract.pytesseract.tesseract_cmd = r"C:\\Program Files\\Tesseract-OCR\\tesseract.exe"

def preprocess_image(image):
    # Converter para escala de cinza
    gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Aplicar CLAHE para melhorar o contraste
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    enhanced_image = clahe.apply(gray_image)

    # Redimensionar para melhorar a precisão
    scale_factor = 2
    resized_image = cv2.resize(enhanced_image, (0, 0), fx=scale_factor, fy=scale_factor, interpolation=cv2.INTER_LINEAR)

    # Aplicar redução de ruído
    denoised_image = cv2.fastNlMeansDenoising(resized_image, None, 30, 7, 21)

    # Aplicar limiar adaptativo para binarizar a imagem
    threshold_image = cv2.adaptiveThreshold(
        denoised_image, 255, cv2.ADAPTIVE_THRESH_GAUSSIAN_C, cv2.THRESH_BINARY, 15, 2
    )

    return threshold_image

def read_text_from_image(image_path):
    # Carregar a imagem
    image = cv2.imread(image_path)

    # Pré-processar a imagem
    processed_image = preprocess_image(image)

    # Rotacionar se necessário (opcional para textos tortos)
    processed_image = rotate_bound(processed_image, 0)  # Ajuste o ângulo conforme necessário

    # Configuração personalizada do Tesseract
    custom_config = "--oem 3 --psm 6 -l por"
    
    # Aplicar OCR na imagem
    text = pytesseract.image_to_string(processed_image, config=custom_config)

    return text

if __name__ == "__main__":
    # Caminho da imagem para teste
    image_path = "imagem-nitida.png"  # Substitua pelo caminho da sua imagem

    # Ler o texto da imagem
    result = read_text_from_image(image_path)

    # Exibir o texto no console
    print("Texto encontrado na imagem:")
    print(result)
