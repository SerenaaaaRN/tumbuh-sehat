import { useChildStore } from '@/features/children/stores/childStore';
import { useChildAssessments } from '@/features/assessment/hooks/useAssessment';
import { useChildrenList } from '@/features/children/hooks/useChildren';

export const useActivePredictionForChat = () => {
  const { activeChildId } = useChildStore();
  const { data: childrenData, isLoading: isChildrenLoading } = useChildrenList();

  // Gunakan activeChildId dari store jika ada, jika tidak fallback ke anak pertama dari childrenData
  const resolvedChildId = activeChildId || childrenData?.data?.[0]?.id;

  const { data: assessmentData, isLoading: isAssessmentLoading } = useChildAssessments(resolvedChildId ?? '', 0, 10);

  if (isChildrenLoading) return { predictionId: null, isLoading: true, activeChildId: resolvedChildId };
  if (!resolvedChildId) return { predictionId: null, isLoading: false, activeChildId: null };
  if (isAssessmentLoading) return { predictionId: null, isLoading: true, activeChildId: resolvedChildId };

  const assessments = assessmentData?.data ?? [];
  const completedAssessment = assessments.find(
    (a) => a.prediction?.predictionStatus === 'COMPLETED'
  );

  return {
    predictionId: completedAssessment?.prediction?.id ?? null,
    isLoading: false,
    activeChildId: resolvedChildId,
  };
};
