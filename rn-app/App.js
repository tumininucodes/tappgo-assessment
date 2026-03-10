import React from 'react';
import { StyleSheet, Text, View, ScrollView, Dimensions, Image } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { registerRootComponent } from 'expo';

const { width } = Dimensions.get('window');

function App() {
    const steps = [
        {
            title: 'Long Press',
            desc: 'Go to your home screen and long-press on any empty space or the app icon.',
        },
        {
            title: 'Open Widgets',
            desc: 'Tap the Widgets (or +) button that appears in the menu.',
        },
        {
            title: 'Find SpinWheel',
            desc: 'Search for "SpinWheel App" and select the widget.',
        },
        {
            title: 'Add to Home',
            desc: 'Drag the widget to your desired location on the home screen.',
        },
    ];

    return (
        <View style={styles.container}>
            <StatusBar style="light" />
            <View style={styles.header}>
                <Image
                    source={require('./assets/icon.png')}
                    style={styles.logo}
                    resizeMode="contain"
                />
            </View>

            <ScrollView contentContainerStyle={styles.scrollContent}>
                <View style={styles.infoCard}>
                    <Text style={styles.infoTitle}>Setup Guide</Text>
                    <Text style={styles.infoText}>
                        Follow these steps to add the spin wheel widget to your homescreen:
                    </Text>
                </View>

                {steps.map((step, index) => (
                    <View key={index} style={styles.stepCard}>
                        <View style={styles.stepTextContent}>
                            <Text style={styles.stepTitle}>{step.title}</Text>
                            <Text style={styles.stepDesc}>{step.desc}</Text>
                        </View>
                    </View>
                ))}
            </ScrollView>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#0a0a0c',
    },
    header: {
        paddingTop: 80,
        paddingHorizontal: 30,
        paddingBottom: 30,
        backgroundColor: '#121217',
        borderBottomLeftRadius: 30,
        borderBottomRightRadius: 30,
    },
    logo: {
        width: 120,
        height: 120,
        alignSelf: 'center',
        borderRadius: 25,
    },
    scrollContent: {
        padding: 20,
        paddingBottom: 100,
    },
    infoCard: {
        backgroundColor: 'rgba(255, 255, 255, 0.05)',
        borderRadius: 20,
        padding: 24,
        marginBottom: 20,
        borderWidth: 1,
        borderColor: 'rgba(255, 255, 255, 0.1)',
    },
    infoTitle: {
        fontSize: 22,
        fontWeight: '700',
        color: '#ff3e8d',
        marginBottom: 10,
    },
    infoText: {
        fontSize: 16,
        color: '#d0d0e0',
        lineHeight: 24,
    },
    stepCard: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: 'rgba(255, 255, 255, 0.03)',
        borderRadius: 18,
        padding: 20,
        marginBottom: 15,
        borderWidth: 1,
        borderColor: 'rgba(255, 255, 255, 0.05)',
    },
    stepTextContent: {
        flex: 1,
    },
    stepTitle: {
        fontSize: 18,
        fontWeight: '800',
        color: '#ffffff',
        marginBottom: 4,
    },
    stepDesc: {
        fontSize: 14,
        color: '#a0a0b0',
        lineHeight: 20,
    },
});

registerRootComponent(App);
