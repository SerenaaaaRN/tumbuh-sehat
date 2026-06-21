import React, { useState } from 'react';
import { View, Text, Pressable, ActivityIndicator, ScrollView } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import * as WebBrowser from 'expo-web-browser';
import { useChildrenList } from '@/features/children/hooks/useChildren';
import { useDownloadReport } from '@/features/report/hooks/useReport';

export const ReportScreen = () => {
  const [selectedChildId, setSelectedChildId] = useState<string | null>(null);
  const { data: childrenData, isLoading: loadingChildren } = useChildrenList();
  const generateReport = useDownloadReport();
  const children = childrenData?.data ?? [];

  const handleDownload = () => {
    if (!selectedChildId) return;
    generateReport.mutate({ childId: selectedChildId }, {
      onSuccess: (url) => WebBrowser.openBrowserAsync(url),
    });
  };

  if (loadingChildren) return <SafeAreaView className="flex-1 bg-background items-center justify-center"><ActivityIndicator size="large" color="#3e646a" /></SafeAreaView>;

  return (
    <SafeAreaView className="flex-1 bg-background p-6">
      <Text className="text-xl font-bold mb-4">Unduh Laporan Anak</Text>
      <Text className="text-sm text-outline mb-4">Pilih anak untuk mengunduh laporan PDF:</Text>
      <ScrollView className="flex-row flex-wrap gap-2 mb-6">
        {children.map((child) => (
          <Pressable key={child.id} onPress={() => setSelectedChildId(child.id)}
            className={`px-4 py-2 rounded-full ${selectedChildId === child.id ? 'bg-primary' : 'bg-surface-low border border-outline-variant/30'}`}>
            <Text className={selectedChildId === child.id ? 'text-white font-bold' : 'text-on-surface'}>{child.name}</Text>
          </Pressable>
        ))}
      </ScrollView>
      <Pressable onPress={handleDownload} disabled={!selectedChildId || generateReport.isPending}
        className={`py-3 rounded-full items-center ${selectedChildId && !generateReport.isPending ? 'bg-primary' : 'bg-outline-variant'}`}>
        {generateReport.isPending ? <ActivityIndicator color="#fff" /> : <Text className="text-white font-bold">Unduh PDF</Text>}
      </Pressable>
    </SafeAreaView>
  );
};
