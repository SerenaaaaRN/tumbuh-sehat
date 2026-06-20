import React from 'react';
import { View, Text, ActivityIndicator, Pressable, Alert } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { router } from 'expo-router';
import * as WebBrowser from 'expo-web-browser';
import { useVerifyBlockchain } from '../hooks/useBlockchain';

export const BlockchainVerificationScreen = ({ assessmentId }: { assessmentId: string }) => {
  const { data, isLoading, isError } = useVerifyBlockchain(assessmentId);

  if (isLoading) return <SafeAreaView className="flex-1 bg-background items-center justify-center"><ActivityIndicator size="large" color="#3e646a" /></SafeAreaView>;
  if (isError || !data) return (
    <SafeAreaView className="flex-1 bg-background items-center justify-center p-6">
      <Text className="text-danger font-bold">Gagal memverifikasi blockchain</Text>
      <Pressable onPress={() => router.back()} className="mt-4 bg-primary px-4 py-2 rounded-full"><Text className="text-white">Kembali</Text></Pressable>
    </SafeAreaView>
  );

  const openExplorer = async () => {
    if (!data.explorerUrl) return Alert.alert('Error', 'URL Explorer tidak tersedia');
    await WebBrowser.openBrowserAsync(data.explorerUrl);
  };

  return (
    <SafeAreaView className="flex-1 bg-background p-6">
      <View className={`p-4 rounded-2xl mb-4 ${data.isValid ? 'bg-secondary/10 border border-secondary/20' : 'bg-danger/10 border border-danger/20'}`}>
        <Text className={`text-lg font-bold ${data.isValid ? 'text-secondary' : 'text-danger'}`}>{data.isValid ? '✓ Terverifikasi' : '✗ Tidak Valid'}</Text>
        <Text className="text-sm text-outline mt-1">Assessment ID: {assessmentId}</Text>
      </View>
      <View className="bg-surface-low p-4 rounded-2xl mb-4">
        <Text className="text-xs text-outline mb-1">Record Hash</Text>
        <Text className="text-sm font-mono">{data.recordHash || '-'}</Text>
        <Text className="text-xs text-outline mt-3 mb-1">Transaction Hash</Text>
        <Text className="text-sm font-mono">{data.txHash || '-'}</Text>
        <Text className="text-xs text-outline mt-3 mb-1">Block Number</Text>
        <Text className="text-sm">{data.blockNumber || '-'}</Text>
      </View>
      <Pressable onPress={openExplorer} disabled={!data.explorerUrl}
        className={`py-3 rounded-full items-center ${data.explorerUrl ? 'bg-primary' : 'bg-outline-variant opacity-50'}`}>
        <Text className="text-white font-bold">Buka di Polygonscan</Text>
      </Pressable>
    </SafeAreaView>
  );
};
