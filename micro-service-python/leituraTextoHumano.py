import cv2
import pytesseract
import numpy as np
from PIL import Image
from skimage import filters
from skimage.restoration import denoise_bilateral
from imutils import rotate_bound

# Configurar o caminho do executável do Tesseract (somente no Windows)
pytesseract.pytesseract.tesseract_cmd = r"C:\\Program Files\\Tesseract-OCR\\tesseract.exe"

def preprocess_image(image):
    # Converter para escala de cinza
    gray_image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Aplicar filtro bilateral para suavizar mantendo bordas
    denoised_image = denoise_bilateral(gray_image, sigma_color=0.05, sigma_spatial=15)

    # Aplicar CLAHE para melhorar o contraste
    clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
    enhanced_image = clahe.apply((denoised_image * 255).astype('uint8'))

    # Aplicar filtro de Sobel para realçar bordas
    sobel_image = filters.sobel(enhanced_image)

    # Binarizar a imagem
    threshold_image = (sobel_image > filters.threshold_otsu(sobel_image)).astype(np.uint8) * 255

    # Redimensionar para melhorar a precisão
    scale_factor = 2
    resized_image = cv2.resize(threshold_image, (0, 0), fx=scale_factor, fy=scale_factor, interpolation=cv2.INTER_LINEAR)

    return resized_image

def read_text_from_image(image_path):
    # Carregar a imagem
    image = cv2.imread(image_path)

    # Pré-processar a imagem
    processed_image = preprocess_image(image)

    # Rotacionar a imagem automaticamente (opcional)
    processed_image = rotate_bound(processed_image, 0)  # Ajuste manual de ângulo, se necessário

    # Salvar a imagem processada para depuração
    processed_pil = Image.fromarray(processed_image)
    processed_pil.save("processed_image_debug.png")

    # Configuração personalizada do Tesseract
    custom_config = "--oem 3 --psm 11 -l por"

    # Aplicar OCR na imagem
    text = pytesseract.image_to_string(processed_image, config=custom_config)

    return text

if __name__ == "__main__":
    # Caminho da imagem para teste
    image_path = "imagens.png"  # Substitua pelo caminho da sua imagem

    # Ler o texto da imagem
    result = read_text_from_image(image_path)

    # Exibir o texto no console
    print("Texto encontrado na imagem:")
    print(result)
