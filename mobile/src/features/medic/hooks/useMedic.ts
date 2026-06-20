import { useQuery } from '@tanstack/react-query';
import { medicService } from '../services/medic.service';

export const useMedicPatients = () => {
  return useQuery({
    queryKey: ['medicPatients'],
    queryFn: medicService.getPatients,
  });
};

// Alias for compatibility
export const usePatients = useMedicPatients;

// Re-export VC hooks for convenience
export { useIssueVc, useRevokeVc } from '@/features/vc/hooks/useVC';
