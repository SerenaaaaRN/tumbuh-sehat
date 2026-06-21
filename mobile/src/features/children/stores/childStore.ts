import { create } from 'zustand';

type ChildState = {
  activeChildId: string | null;
  setActiveChildId: (id: string | null) => void;
};

export const useChildStore = create<ChildState>((set) => ({
  activeChildId: null,
  setActiveChildId: (activeChildId) => set({ activeChildId }),
}));
