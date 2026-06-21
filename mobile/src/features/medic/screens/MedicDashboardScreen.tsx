import React, { useState, useMemo } from 'react';
import { View, Text, TextInput, ActivityIndicator, Alert } from 'react-native';
import { FlashList } from '@shopify/flash-list';
import { SafeAreaView } from 'react-native-safe-area-context';
import { router } from 'expo-router';
import { PatientCard } from '../components/PatientCard';
import { usePatients, useIssueVc, useRevokeVc } from '../hooks/useMedic';
import type { Patient, PatientStatus, PatientSearchFilter } from '../types/medic.types';

export const MedicDashboardScreen = () => {
  const [filter, setFilter] = useState<PatientSearchFilter>({ search: '', status: 'ALL' });
  const [activeActionId, setActiveActionId] = useState<string | null>(null);
  const [actionType, setActionType] = useState<'issuing' | 'revoking' | 'none'>('none');

  const { data, isLoading, isError, refetch } = usePatients();
  const issueVcMutation = useIssueVc();
  const revokeVcMutation = useRevokeVc();

  const patients = useMemo(() => {
    const raw = data?.data ?? [];
    return raw.filter(p => {
      const matchSearch = p.childName.toLowerCase().includes(filter.search.toLowerCase());
      const matchStatus = filter.status === 'ALL' || p.lastStatus === filter.status;
      return matchSearch && matchStatus;
    });
  }, [data, filter]);

  const handlePress = (patient: Patient) => {
    router.push(`/(app)/children/${patient.childId}`);
  };

  const handleIssueVc = (patient: Patient) => {
    Alert.alert('Terbitkan VC', `Terbitkan Verifiable Credential untuk ${patient.childName}?`, [
      { text: 'Batal', style: 'cancel' },
      { text: 'Terbitkan', onPress: () => {
          setActiveActionId(patient.childId);
          setActionType('issuing');
          issueVcMutation.mutate({ childId: patient.childId, type: 'ASSESSMENT_RESULT' }, {
            onSettled: () => { setActiveActionId(null); setActionType('none'); }
          });
        }
      }
    ]);
  };

  const handleRevokeVc = (patient: Patient) => {
    if (!patient.activeVcId) return Alert.alert('Error', 'ID VC tidak ditemukan');
    Alert.alert('Cabut VC', `Cabut Verifiable Credential untuk ${patient.childName}?`, [
      { text: 'Batal', style: 'cancel' },
      { text: 'Cabut', style: 'destructive', onPress: () => {
          setActiveActionId(patient.childId);
          setActionType('revoking');
          revokeVcMutation.mutate({ vcId: patient.activeVcId! }, {
            onSettled: () => { setActiveActionId(null); setActionType('none'); }
          });
        }
      }
    ]);
  };

  if (isLoading) return <SafeAreaView className="flex-1 bg-background items-center justify-center"><ActivityIndicator size="large" color="#3e646a" /></SafeAreaView>;

  return (
    <SafeAreaView className="flex-1 bg-background">
      <View className="px-6 pt-6 pb-4">
        <Text className="text-xl font-bold text-on-surface">Dashboard Medis</Text>
      </View>
      <View className="px-6 mb-4">
        <TextInput placeholder="Cari nama anak..." value={filter.search} onChangeText={(txt) => setFilter(prev => ({ ...prev, search: txt }))}
          className="bg-surface-low px-4 py-2 rounded-full border border-outline-variant/30" />
      </View>
      <FlashList data={patients} keyExtractor={(item) => item.childId} estimatedItemSize={150}
        renderItem={({ item }) => (
          <PatientCard patient={item} onPress={handlePress} onIssueVc={handleIssueVc} onRevokeVc={handleRevokeVc}
            vcAction={activeActionId === item.childId ? actionType : 'none'} />
        )}
        contentContainerStyle={{ paddingHorizontal: 24, paddingBottom: 100 }}
        ListEmptyComponent={<Text className="text-center text-outline mt-10">Tidak ada pasien</Text>} />
    </SafeAreaView>
  );
};
