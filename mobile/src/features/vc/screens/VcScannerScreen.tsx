import React, { useState, useRef } from 'react';
import { Pressable, Text, View, Alert, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import Animated from 'react-native-reanimated';
import { router } from 'expo-router';
import { CameraView, useCameraPermissions } from 'expo-camera';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { useBounceAnimation } from '@/hooks/useBounceAnimation';
import { useVerifyVc } from '@/features/vc';

export const VcScannerScreen = () => {
  const [isScanning, setIsScanning] = useState(false);
  const [permission, requestPermission] = useCameraPermissions();
  const cameraRef = useRef<CameraView>(null);

  const animatedIconStyle = useBounceAnimation();
  const verifyVc = useVerifyVc();

  if (!permission) {
    return <View className="flex-1 bg-black" />;
  }

  if (!permission.granted) {
    return (
      <View className="flex-1 bg-black items-center justify-center px-6">
        <Text className="text-white text-center mb-6">
          Kami membutuhkan izin kamera Anda untuk memindai QR Code Verifiable Credential.
        </Text>
        <Pressable
          onPress={requestPermission}
          className="bg-primary px-6 py-3 rounded-full"
        >
          <Text className="text-white font-bold">Beri Izin Kamera</Text>
        </Pressable>
      </View>
    );
  }

  const handleBarCodeScanned = async ({ data }: { data: string }) => {
    if (isScanning) return;

    setIsScanning(true);

    try {
      // Data dari QR biasanya berupa credentialHash atau URL
      // Extract hash dari URL jika perlu
      const credentialHash = data.includes('hash=')
        ? data.split('hash=')[1]?.split('&')[0] ?? data
        : data;

      const result = await verifyVc.mutateAsync(credentialHash);

      router.push({
        pathname: '/(app)/vc/verify-result',
        params: {
          verified: result.isValid.toString(),
          message: result.message,
          vcId: result.vc?.id ?? '',
        },
      });
    } catch (error) {
      Alert.alert('Error', 'Gagal memverifikasi credential. Silakan coba lagi.');
    } finally {
      setIsScanning(false);
    }
  };

  const handleGoBack = () => {
    router.back();
  };

  return (
    <SafeAreaView className="flex-1 bg-black">
      {/* Header */}
      <View className="flex-row justify-between items-center px-6 py-4 absolute top-12 left-0 right-0 z-10">
        <Pressable onPress={handleGoBack} className="w-10 h-10 rounded-full bg-black/45 items-center justify-center">
          <IconSymbol name="arrow.left" size={20} color="#ffffff" />
        </Pressable>
        <Text className="text-white font-bold text-base">Scan QR Credential</Text>
        <View className="w-10 h-10" />
      </View>

      {/* Camera Viewfinder */}
      <View className="flex-1 rounded-3xl overflow-hidden m-4 mt-24 mb-4 relative">
        <CameraView
          ref={cameraRef}
          style={StyleSheet.absoluteFillObject}
          facing="back"
          onBarcodeScanned={isScanning ? undefined : handleBarCodeScanned}
          barcodeScannerSettings={{
            barcodeTypes: ['qr'],
          }}
        />

        {/* Overlay Frame */}
        <View className="absolute inset-0 items-center justify-center pointer-events-none">
          {isScanning ? (
            <View className="items-center gap-3 bg-black/80 p-6 rounded-[32px] border border-white/10">
              <Animated.Text className="text-4xl" style={animatedIconStyle}>🔍</Animated.Text>
              <Text className="text-white font-bold text-sm">Memverifikasi...</Text>
              <Text className="text-xs text-gray-400">Mengecek blockchain</Text>
            </View>
          ) : (
            <View className="w-72 h-72 border-2 border-primary border-dashed rounded-[32px] items-center justify-center bg-black/10">
              <IconSymbol name="qrcode.viewfinder" size={48} color="rgba(255,255,255,0.5)" />
            </View>
          )}
        </View>
      </View>

      {/* Instructions */}
      <View className="px-6 pb-12 pt-6 bg-black gap-4">
        <Text className="text-zinc-400 text-xs text-center leading-4 px-4">
          Arahkan kamera ke QR Code Verifiable Credential untuk memverifikasi keaslian data kesehatan anak.
        </Text>

        <View className="flex-row justify-center">
          <Pressable
            onPress={handleGoBack}
            className="bg-zinc-800 px-6 py-3 rounded-full"
          >
            <Text className="text-white font-bold text-sm">Batal</Text>
          </Pressable>
        </View>
      </View>
    </SafeAreaView>
  );
};
