import { delay, USE_MOCK } from '@/services/mock';
import type { PosyanduSession } from '../types/posyandu.types';

const mockSessions: PosyanduSession[] = [
  { id: 'pos_001', date: '2026-07-01', location: 'Posyandu Melati', posyanduName: 'Melati', status: 'SCHEDULED', registeredChildren: 0 },
  { id: 'pos_002', date: '2026-06-15', location: 'Posyandu Mawar', posyanduName: 'Mawar', status: 'COMPLETED', registeredChildren: 12 },
];

const mockGetSessions = async (): Promise<PosyanduSession[]> => { await delay(400); return mockSessions; };

const realGetSessions = async (): Promise<PosyanduSession[]> => {
  const { apiClient } = await import('@/services/api');
  const res = await apiClient.get<PosyanduSession[]>('/api/posyandu/sessions');
  return res.data;
};

export const posyanduService = {
  getSessions: USE_MOCK ? mockGetSessions : realGetSessions,
};
