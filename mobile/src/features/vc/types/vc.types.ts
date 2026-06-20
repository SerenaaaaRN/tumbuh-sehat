export type VcType = 'ASSESSMENT_RESULT' | 'GROWTH_MILESTONE' | 'NUTRITION_LOG';
export type VcStatus = 'ACTIVE' | 'REVOKED' | 'EXPIRED' | 'PENDING';

export type VcSubject = {
  id: string;
  name: string;
  ageMonths: number;
};

export type VcClaims = {
  weight?: number;
  height?: number;
  status?: string;
  assessmentDate?: string;
};

export type VcRecord = {
  id: string;
  childId: string;
  childName: string;
  type: VcType;
  status: VcStatus;
  issuedAt: string;
  issuanceDate?: string;
  expiresAt?: string;
  revokedAt?: string;
  issuer: { id: string; name: string; role: 'MEDIC' | 'ADMIN' };
  ipfsCid: string;
  txHash: string;
  blockNumber?: number;
  credentialHash?: string;
  subject?: VcSubject;
  claims?: VcClaims;
};

export type VcIssueRequest = { childId: string; type: VcType; assessmentId?: string };
export type VcRevokeRequest = { vcId: string; reason?: string };
export type VcVerificationResult = { isValid: boolean; vc: VcRecord | null; verifiedAt: string; message: string };
