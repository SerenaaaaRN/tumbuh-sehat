import { apiClient } from '@/services/api';
import { delay, USE_MOCK } from '@/services/mock';
import type { VcRecord, VcIssueRequest, VcRevokeRequest, VcVerificationResult } from '../types/vc.types';

const mockVcRecords: Record<string, VcRecord> = {
  'vc_001': {
    id: 'vc_001', childId: 'child_001', childName: 'Andi Santoso', type: 'ASSESSMENT_RESULT',
    status: 'ACTIVE', issuedAt: '2024-01-15T10:30:00Z', issuanceDate: '2024-01-15',
    issuer: { id: 'user_medic_001', name: 'Dr. Siti Nurhaliza', role: 'MEDIC' },
    ipfsCid: 'QmX4j...abc123', txHash: '0x1a2b3c...def456', blockNumber: 12345678,
    subject: { id: 'child_001', name: 'Andi Santoso', ageMonths: 18 },
    claims: { weight: 9.5, height: 75, status: 'NORMAL', assessmentDate: '2024-01-15' },
  },
};

const mockGetVcStatus = async (childId: string): Promise<VcRecord | null> => {
  await delay(400);
  return Object.values(mockVcRecords).find(v => v.childId === childId && v.status === 'ACTIVE') || null;
};

const mockGetVcDetail = async (vcId: string): Promise<VcRecord | null> => {
  await delay(350);
  return mockVcRecords[vcId] || null;
};

const mockVerifyVc = async (vcId: string): Promise<VcVerificationResult> => {
  await delay(350);
  const vc = mockVcRecords[vcId];
  if (!vc) return { isValid: false, vc: null, verifiedAt: new Date().toISOString(), message: 'VC tidak ditemukan' };
  return { isValid: vc.status === 'ACTIVE', vc, verifiedAt: new Date().toISOString(), message: 'Verifikasi berhasil' };
};

const mockIssueVc = async (request: VcIssueRequest): Promise<VcRecord> => {
  await delay(600);
  const newVc: VcRecord = {
    id: `vc_${Date.now()}`, childId: request.childId, childName: 'Anak', type: request.type,
    status: 'ACTIVE', issuedAt: new Date().toISOString(), issuanceDate: new Date().toISOString().split('T')[0],
    issuer: { id: 'user_medic_001', name: 'Dr. Siti', role: 'MEDIC' },
    ipfsCid: 'Qm...', txHash: '0x...', blockNumber: Math.floor(Math.random() * 1000000),
  };
  mockVcRecords[newVc.id] = newVc;
  return newVc;
};

const mockRevokeVc = async (request: VcRevokeRequest): Promise<void> => {
  await delay(400);
  if (mockVcRecords[request.vcId]) {
    mockVcRecords[request.vcId].status = 'REVOKED';
    mockVcRecords[request.vcId].revokedAt = new Date().toISOString();
  }
};

const realGetVcStatus = async (childId: string): Promise<VcRecord | null> => {
  try { const res = await apiClient.get<{ vc: VcRecord | null }>(`/api/vc/child/${childId}`); return res.data.vc; }
  catch { return null; }
};
const realGetVcDetail = async (vcId: string): Promise<VcRecord | null> => {
  const res = await apiClient.get<VcRecord>(`/api/vc/${vcId}`); return res.data;
};
const realVerifyVc = async (vcId: string) => (await apiClient.get<VcVerificationResult>(`/api/vc/${vcId}/verify`)).data;
const realIssueVc = async (request: VcIssueRequest) => (await apiClient.post<VcRecord>('/api/vc/issue', request)).data;
const realRevokeVc = async (request: VcRevokeRequest) => { await apiClient.post('/api/vc/revoke', request); };

export const vcService = {
  getVcStatus: USE_MOCK ? mockGetVcStatus : realGetVcStatus,
  getVcDetail: USE_MOCK ? mockGetVcDetail : realGetVcDetail,
  verifyVc: USE_MOCK ? mockVerifyVc : realVerifyVc,
  issueVc: USE_MOCK ? mockIssueVc : realIssueVc,
  revokeVc: USE_MOCK ? mockRevokeVc : realRevokeVc,
};
