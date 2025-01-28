import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

const ExamplePage = () => {
    return (
        <View style={styles.container}>
            <Text style={styles.title}>Bem-vindo à Página de Exemplo!</Text>
            <Text style={styles.text}>Aqui você pode adicionar conteúdos personalizados.</Text>
        </View>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#f0f0f0',
    },
    title: {
        fontSize: 24,
        fontWeight: 'bold',
        marginBottom: 16,
    },
    text: {
        fontSize: 16,
    },
});

export default ExamplePage;
