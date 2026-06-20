import { HomeScreen } from '@/features/home';
import { MedicDashboardScreen } from '@/features/medic/screens/MedicDashboardScreen';
import { useAuthStore } from '@/stores/authStore';

const Page = () => {
  const role = useAuthStore((s) => s.user?.role);

  if (role === 'MEDIC') {
    return <MedicDashboardScreen />;
  }

  return <HomeScreen />;
};

export default Page;
