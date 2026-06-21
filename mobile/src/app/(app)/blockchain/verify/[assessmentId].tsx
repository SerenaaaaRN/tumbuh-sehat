import { useLocalSearchParams } from 'expo-router';
import { BlockchainVerificationScreen } from '@/features/blockchain/screens/BlockchainVerificationScreen';

export default function VerifyRoute() {
  const { assessmentId } = useLocalSearchParams<{ assessmentId: string }>();
  return <BlockchainVerificationScreen assessmentId={assessmentId ?? ''} />;
}
