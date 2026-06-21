import React from 'react';
import { View, Text, ScrollView, ActivityIndicator } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { router, useLocalSearchParams } from 'expo-router';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { IconSymbol } from '@/components/ui/icon-symbol';
import { useVcDetail } from '@/features/vc/hooks/useVC';

export const VcVerificationResultScreen = () => {
  const { verified, message, vcId } = useLocalSearchParams<{
    verified: string;
    message: string;
    vcId: string;
  }>();

  const isVerified = verified === 'true';
  const hasVcData = !!vcId && vcId !== 'undefined' && vcId !== '';

  const { data: vc, isLoading } = useVcDetail(hasVcData ? vcId : '');

  const handleDone = () => {
    router.dismissAll();
    router.replace('/(app)/(tabs)/' as any);
  };

  return (
    <SafeAreaView className="flex-1 bg-background" edges={['top', 'bottom']}>
      {/* Header */}
      <View className="px-container-padding py-4 border-b border-surface-container bg-surface-lowest items-center">
        <Text className="font-bold text-lg text-primary">Hasil Verifikasi</Text>
      </View>

      <ScrollView contentContainerStyle={{ padding: 24, gap: 20 }} showsVerticalScrollIndicator={false}>
        {/* Status Card */}
        <View className="items-center py-6 gap-3 bg-surface-lowest rounded-[24px] border border-outline-variant/15 p-5">
          <View className={`w-20 h-20 rounded-full items-center justify-center mb-2 ${isVerified ? 'bg-secondary-container' : 'bg-danger-light'}`}>
            <IconSymbol
              name={isVerified ? 'checkmark' : 'exclamationmark.triangle.fill'}
              size={40}
              color={isVerified ? '#506444' : '#ba1a1a'}
            />
          </View>
          <Text className={`font-extrabold text-xl ${isVerified ? 'text-secondary' : 'text-danger'}`}>
            {isVerified ? 'Data Valid & Asli' : 'Verifikasi Gagal'}
          </Text>
          <Text className="text-sm text-outline text-center leading-5 px-4 font-medium">
            {message}
          </Text>
        </View>

        {/* VC Data */}
        {isLoading ? (
          <Card className="p-8 items-center justify-center">
            <ActivityIndicator size="large" color="#3e646a" />
            <Text className="text-xs text-outline mt-4 font-bold">Memuat detail credential...</Text>
          </Card>
        ) : vc && isVerified ? (
          <View className="gap-2">
            <Text className="font-bold text-xs text-outline uppercase tracking-wider">Detail Credential</Text>
            <Card className="p-4 gap-4">
              <View className="flex-row justify-between">
                <Text className="text-xs text-outline font-medium">Anak</Text>
                <Text className="text-xs font-bold text-on-surface">{vc.subject.name}</Text>
              </View>
              <View className="flex-row justify-between">
                <Text className="text-xs text-outline font-medium">Usia Saat Pemeriksaan</Text>
                <Text className="text-xs font-bold text-on-surface">{vc.subject.ageMonths} Bulan</Text>
              </View>
              <View className="flex-row justify-between">
                <Text className="text-xs text-outline font-medium">Penerbit</Text>
                <Text className="text-xs font-bold text-on-surface">{vc.issuer.name}</Text>
              </View>
              <View className="flex-row justify-between">
                <Text className="text-xs text-outline font-medium">Status Kesehatan</Text>
                <Text className="text-xs font-bold text-primary">
                  {vc.claims.status === 'NORMAL' ? 'Normal' :
                   vc.claims.status === 'AT_RISK' ? 'Berisiko' :
                   vc.claims.status === 'STUNTED' ? 'Stunting' : 'Stunting Berat'}
                </Text>
              </View>

              <View className="pt-3 border-t border-surface-container mt-1 gap-3">
                <View className="flex-row justify-between">
                  <Text className="text-xs text-outline font-medium">TX Hash</Text>
                  <Text className="text-[10px] font-mono text-on-surface max-w-[200px]" numberOfLines={1} ellipsizeMode="middle">
                    {vc.txHash}
                  </Text>
                </View>
                <View className="flex-row justify-between">
                  <Text className="text-xs text-outline font-medium">Block Number</Text>
                  <Text className="text-xs font-mono text-on-surface">#{vc.blockNumber}</Text>
                </View>
                <View className="flex-row justify-between">
                  <Text className="text-xs text-outline font-medium">Tanggal Terbit</Text>
                  <Text className="text-xs font-bold text-on-surface">
                    {new Date(vc.issuanceDate).toLocaleDateString('id-ID')}
                  </Text>
                </View>
              </View>
            </Card>
          </View>
        ) : null}

        {/* Security Notice */}
        <View className="flex-row items-center gap-2 p-4 bg-primary/5 border border-primary/10 rounded-[20px] mt-2 mb-4">
          <IconSymbol name="shield.fill" size={16} color="#3e646a" />
          <Text className="text-[11px] font-semibold text-primary flex-1 leading-4">
            Keaslian dokumen ini dijamin oleh jaringan blockchain Polygon. Data tidak dapat dipalsukan atau diubah oleh pihak manapun.
          </Text>
        </View>

        {/* Actions */}
        <Button onPress={handleDone} variant="primary" className="mt-2">
          Selesai
        </Button>
      </ScrollView>
    </SafeAreaView>
  );
};
