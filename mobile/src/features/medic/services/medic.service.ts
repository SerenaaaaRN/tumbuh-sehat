import { apiClient } from '@/services/api';
import { delay, USE_MOCK } from '@/services/mock';
import type { Patient } from '../types/medic.types';

const mockPatients: Patient[] = [
  {
    childId: 'child_001',
    childName: 'Andi Santoso',
    childAgeMonths: 18,
    lastAssessmentDate: '2024-01-15',
    lastStatus: 'AT_RISK',
    hasActiveVc: true,
    activeVcId: 'vc_001',
    parentId: 'user_001',
    parentName: 'Budi Santoso',
  },
  {
    childId: 'child_002',
    childName: 'Sari Dewi',
    childAgeMonths: 12,
    lastAssessmentDate: '2024-01-10',
    lastStatus: 'NORMAL',
    hasActiveVc: false,
    parentId: 'user_001',
    parentName: 'Budi Santoso',
  },
];

const mockGetPatients = async (): Promise<{ data: Patient[] }> => {
  await delay(400);
  return { data: mockPatients };
};

const realGetPatients = async (): Promise<{ data: Patient[] }> => {
  const res = await apiClient.get<{ data: Patient[] }>('/api/medic/patients');
  return res.data;
};

export const medicService = {
  getPatients: USE_MOCK ? mockGetPatients : realGetPatients,
};
