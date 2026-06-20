import { apiClient } from '@/services/api';
import { delay, USE_MOCK, getMockReportUrl } from '@/services/mock';
import type { ReportRequest } from '../types/report.types';
import * as FileSystem from 'expo-file-system';
import * as Sharing from 'expo-sharing';

const mockGenerateReport = async (request: ReportRequest): Promise<string> => {
  await delay(800);
  return getMockReportUrl(request.childId, request.from, request.to);
};

const realGenerateReport = async (request: ReportRequest): Promise<string> => {
  // In real implementation on mobile, we can't use blob URLs easily with WebBrowser.
  // Instead, download to filesystem and share it.
  const params = new URLSearchParams();
  if (request.from) params.set('from', request.from);
  if (request.to) params.set('to', request.to);
  const qs = params.toString();
  
  // Create download url
  const { EXPO_PUBLIC_API_URL } = process.env;
  const baseUrl = EXPO_PUBLIC_API_URL || 'http://localhost:8080';
  const downloadUrl = `${baseUrl}/api/reports/child/${request.childId}${qs ? `?${qs}` : ''}`;
  
  // We'll return the URL directly and let the frontend handle the download/sharing
  return downloadUrl;
};

export const reportService = {
  generateReport: USE_MOCK ? mockGenerateReport : realGenerateReport,
};
