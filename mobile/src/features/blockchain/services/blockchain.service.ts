import { apiClient } from '@/services/api';
import { delay, USE_MOCK, getMockBlockchainVerification } from '@/services/mock';
import type { BlockchainVerificationResult } from '../types/blockchain.types';

const mockVerifyAssessment = async (assessmentId: string): Promise<BlockchainVerificationResult> => {
  await delay(500);
  return getMockBlockchainVerification(assessmentId);
};

const realVerifyAssessment = async (assessmentId: string): Promise<BlockchainVerificationResult> => {
  const res = await apiClient.get<BlockchainVerificationResult>(`/api/blockchain/verify/${assessmentId}`);
  return res.data;
};

export const blockchainService = {
  verifyAssessment: USE_MOCK ? mockVerifyAssessment : realVerifyAssessment,
};
