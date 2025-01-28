import React, { useRef, useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Image, ActivityIndicator, Alert } from 'react-native';
import { Camera } from 'expo-camera';
import * as ImagePicker from 'expo-image-picker';
import * as FileSystem from 'expo-file-system';

export default function CapturePhoto() {
    const [hasPermission, setHasPermission] = useState(null);
    const [photoUri, setPhotoUri] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const cameraRef = useRef<Camera>(null);

    // Solicita permissão para usar a câmera e acessar a galeria
    React.useEffect(() => {
        (async () => {
            const { status } = await Camera.requestCameraPermissionsAsync();
            const galleryStatus = await ImagePicker.requestMediaLibraryPermissionsAsync();
            setHasPermission(status === 'granted' && galleryStatus.status === 'granted');
        })();
    }, []);

    // Captura uma foto com a câmera
    const takePhoto = async () => {
        if (cameraRef.current) {
            const photo = await cameraRef.current.takePictureAsync();
            setPhotoUri(photo.uri); // Armazena o caminho da foto
        }
    };

    // Seleciona uma imagem da galeria
    const pickImageFromGallery = async () => {
        try {
            const result = await ImagePicker.launchImageLibraryAsync({
                mediaTypes: ImagePicker.MediaTypeOptions.Images,
                allowsEditing: true,
                quality: 1,
            });

            if (!result.canceled) {
                setPhotoUri(result.assets[0].uri); // Armazena o caminho da imagem selecionada
            }
        } catch (error) {
            Alert.alert('Erro', 'Não foi possível selecionar a imagem.');
            console.error(error);
        }
    };

    // Envia a imagem para a API
    const sendPhotoToAPI = async () => {
        if (!photoUri) return;

        setLoading(true);

        try {
            const apiUrl = 'http://192.168.100.5:5000/process-image'; // Substitua pelo IP da sua API
            const fileInfo = await FileSystem.readAsStringAsync(photoUri, {
                encoding: FileSystem.EncodingType.Base64,
            });

            const response = await fetch(apiUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    image: fileInfo, // Envia a imagem como Base64
                }),
            });

            const data = await response.json();

            if (response.ok) {
                Alert.alert('Sucesso', `Texto extraído: ${data.extracted_text}`);
            } else {
                Alert.alert('Erro', data.error || 'Erro ao processar a imagem.');
            }
        } catch (error) {
            Alert.alert('Erro', 'Erro ao conectar com a API.');
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    if (hasPermission === null) {
        return <Text>Solicitando permissão para usar a câmera e galeria...</Text>;
    }
    if (hasPermission === false) {
        return <Text>Permissão negada para usar a câmera e galeria</Text>;
    }

    return (
        <View style={styles.container}>
            {!photoUri ? (
                <>
                    <Camera ref={cameraRef} style={styles.camera}>
                        <View style={styles.buttonContainer}>
                            <TouchableOpacity style={styles.button} onPress={takePhoto}>
                                <Text style={styles.buttonText}>Tirar Foto</Text>
                            </TouchableOpacity>
                        </View>
                    </Camera>
                    <View style={styles.buttonContainer}>
                        <TouchableOpacity style={styles.button} onPress={pickImageFromGallery}>
                            <Text style={styles.buttonText}>Selecionar da Galeria</Text>
                        </TouchableOpacity>
                    </View>
                </>
            ) : (
                <View style={styles.previewContainer}>
                    <Image source={{ uri: photoUri }} style={styles.previewImage} />
                    {loading ? (
                        <ActivityIndicator size="large" color="#1E90FF" />
                    ) : (
                        <>
                            <TouchableOpacity style={styles.button} onPress={sendPhotoToAPI}>
                                <Text style={styles.buttonText}>Enviar para API</Text>
                            </TouchableOpacity>
                            <TouchableOpacity
                                style={[styles.button, styles.cancelButton]}
                                onPress={() => setPhotoUri(null)}
                            >
                                <Text style={styles.buttonText}>Tirar outra foto ou selecionar</Text>
                            </TouchableOpacity>
                        </>
                    )}
                </View>
            )}
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#000',
    },
    camera: {
        flex: 1,
        justifyContent: 'flex-end',
    },
    buttonContainer: {
        flexDirection: 'row',
        justifyContent: 'center',
        marginBottom: 20,
    },
    button: {
        backgroundColor: '#1E90FF',
        padding: 15,
        borderRadius: 10,
        marginHorizontal: 10,
    },
    cancelButton: {
        backgroundColor: '#FF6347',
    },
    buttonText: {
        color: '#fff',
        fontSize: 16,
        fontWeight: 'bold',
    },
    previewContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#000',
    },
    previewImage: {
        width: '80%',
        height: '60%',
        marginBottom: 20,
        borderRadius: 10,
    },
});
