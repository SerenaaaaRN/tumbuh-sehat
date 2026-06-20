import React from 'react';
import { View, Text, FlatList } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Card } from '@/components/ui/Card';

export const PosyanduListScreen = () => {
  return (
    <SafeAreaView className="flex-1 bg-background p-6">
      <Text className="text-xl font-bold mb-4">Posyandu</Text>
      <Text className="text-sm text-outline">Modul Posyandu akan segera hadir</Text>
    </SafeAreaView>
  );
};
