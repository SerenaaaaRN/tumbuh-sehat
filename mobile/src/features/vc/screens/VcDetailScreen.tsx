import React from 'react';
import { View, Text, ScrollView, Pressable, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { router, useLocalSearchParams } from 'expo-router';
import { Card } from '@/components/ui/Card';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { useVcDetail } from '@/features/vc/hooks/useVC';
import { Button } from '@/components/ui/Button';

export const VcDetailScreen = () => {
  const { vcId } = useLocalSearchParams<{ vcId: string }>();
  const { data: vc, isLoading } = useVcDetail(vcId ?? '');

  const handleGoBack = () => {
    router.back();
  };

  if (isLoading) {
    return (
      <SafeAreaView className="flex-1 bg-background justify-center items-center gap-4">
        <ActivityIndicator size="large" color="#3e646a" />
        <Text className="font-bold text-outline">Memuat detail credential...</Text>
      </SafeAreaView>
    );
  }

  if (!vc) {
    return (
      <SafeAreaView className="flex-1 bg-background justify-center items-center px-6 gap-4">
        <Text className="text-4xl">📄</Text>
        <Text className="font-bold text-lg text-on-surface text-center">Credential Tidak Ditemukan</Text>
        <Button onPress={handleGoBack} variant="outline" className="mt-4">
          Kembali
        </Button>
      </SafeAreaView>
    );
  }

  const isRevoked = vc.status === 'REVOKED';

  return (
    <SafeAreaView className="flex-1 bg-background" edges={['top', 'bottom']}>
      {/* Header */}
      <View className="flex-row items-center px-6 py-4 border-b border-surface-container bg-surface-lowest">
        <Pressable onPress={handleGoBack} className="w-10 h-10 rounded-full bg-surface-low items-center justify-center border border-outline-variant/10 mr-4">
          <IconSymbol name="chevron.left" size={20} color="#3e646a" />
        </Pressable>
        <Text className="font-bold text-lg text-on-surface">Detail Credential</Text>
      </View>

      <ScrollView contentContainerStyle={{ padding: 24, gap: 20 }} showsVerticalScrollIndicator={false}>
        {/* Title */}
        <View className="gap-1.5 mt-2">
          <Text className="text-[26px] font-extrabold text-on-surface tracking-tight">Verifiable Credential</Text>
          <Text className="text-sm text-outline font-medium leading-5">
            Dokumen medis digital yang dijamin oleh blockchain Polygon.
          </Text>
        </View>

        {/* Status Card */}
        <Card className={`p-4 flex-row items-center gap-3 border ${isRevoked ? 'border-danger/30 bg-danger/5' : 'border-primary/30 bg-primary/5'}`}>
          <View className={`w-12 h-12 rounded-full items-center justify-center ${isRevoked ? 'bg-danger-light' : 'bg-primary-light'}`}>
            <IconSymbol name="shield.fill" size={24} color={isRevoked ? '#ba1a1a' : '#3e646a'} />
          </View>
          <View className="flex-1">
            <Text className={`font-bold text-sm ${isRevoked ? 'text-danger-dark' : 'text-primary'}`}>
              Status: {isRevoked ? 'DICABUT (REVOKED)' : 'AKTIF (VERIFIED)'}
            </Text>
            <Text className={`text-xs font-medium mt-0.5 ${isRevoked ? 'text-danger' : 'text-primary'}`}>
              {isRevoked ? vc.claims?.revocationReason || 'Dokumen medis tidak berlaku lagi.' : 'Dokumen medis valid dan sah.'}
            </Text>
          </View>
        </Card>

        {/* Claims Data */}
        <View className="gap-2">
          <Text className="text-xs font-bold text-outline uppercase tracking-wider">Klaim Medis</Text>
          <Card className="p-4 gap-4">
            <View className="flex-row justify-between pt-2 border-b border-surface-container pb-3">
              <View className="items-center flex-1">
                <Text className="text-[10px] text-outline font-medium">Berat Badan</Text>
                <Text className="text-xs font-bold text-on-surface mt-1">{String(vc.claims.weight)} kg</Text>
              </View>
              <View className="w-px h-8 bg-surface-container" />
              <View className="items-center flex-1">
                <Text className="text-[10px] text-outline font-medium">Tinggi Badan</Text>
                <Text className="text-xs font-bold text-on-surface mt-1">{String(vc.claims.height)} cm</Text>
              </View>
              <View className="w-px h-8 bg-surface-container" />
              <View className="items-center flex-1">
                <Text className="text-[10px] text-outline font-medium">Status Gizi</Text>
                <Text className="text-xs font-bold text-primary mt-1">
                  {vc.claims.status === 'NORMAL' ? 'Normal' :
                   vc.claims.status === 'AT_RISK' ? 'Berisiko' :
                   vc.claims.status === 'STUNTED' ? 'Stunting' : 'Stunting Berat'}
                </Text>
              </View>
            </View>

            <View className="gap-3 pt-2">
              <View className="flex-row justify-between">
                <Text className="text-xs text-outline font-medium">Anak</Text>
                <Text className="text-xs font-bold text-on-surface">{vc.subject.name}</Text>
              </View>
              <View className="flex-row justify-between">
                <Text className="text-xs text-outline font-medium">Usia Saat Diperiksa</Text>
                <Text className="text-xs font-bold text-on-surface">{vc.subject.ageMonths} Bulan</Text>
              </View>
              <View className="flex-row justify-between">
                <Text className="text-xs text-outline font-medium">Tanggal Terbit</Text>
                <Text className="text-xs font-bold text-on-surface">{new Date(vc.issuanceDate).toLocaleDateString('id-ID')}</Text>
              </View>
            </View>
          </Card>
        </View>

        {/* Blockchain Record */}
        <View className="gap-2">
          <Text className="text-xs font-bold text-outline uppercase tracking-wider">Blockchain Ledger</Text>
          <Card className="p-4 gap-3">
            <View className="flex-row justify-between">
              <Text className="text-xs text-outline font-medium">Penerbit Dokumen</Text>
              <Text className="text-xs font-bold text-on-surface">{vc.issuer.name}</Text>
            </View>
            <View className="flex-row justify-between">
              <Text className="text-xs text-outline font-medium">Issuer Wallet</Text>
              <Text className="text-[10px] font-mono text-on-surface max-w-[200px]" numberOfLines={1} ellipsizeMode="middle">
                {vc.issuer.walletAddress}
              </Text>
            </View>
            <View className="flex-row justify-between pt-3 border-t border-surface-container">
              <Text className="text-xs text-outline font-medium">Block Number</Text>
              <Text className="text-xs font-bold text-on-surface">#{vc.blockNumber}</Text>
            </View>
            <View className="flex-row justify-between">
              <Text className="text-xs text-outline font-medium">TX Hash</Text>
              <Text className="text-[10px] font-mono text-on-surface max-w-[200px]" numberOfLines={1} ellipsizeMode="middle">
                {vc.txHash}
              </Text>
            </View>
            <View className="flex-row justify-between">
              <Text className="text-xs text-outline font-medium">Credential Hash</Text>
              <Text className="text-[10px] font-mono text-on-surface max-w-[200px]" numberOfLines={1} ellipsizeMode="middle">
                {vc.credentialHash}
              </Text>
            </View>
          </Card>
        </View>

        <Button onPress={handleGoBack} variant="primary" className="mb-8 mt-2">
          Tutup Detail
        </Button>
      </ScrollView>
    </SafeAreaView>
  );
};
