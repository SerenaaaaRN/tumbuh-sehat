import React, { useEffect, useState, useRef } from "react";
import { ActivityIndicator, Text, View, Alert } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import Animated from "react-native-reanimated";
import { router, useLocalSearchParams } from "expo-router";
import { IconSymbol } from "@/components/ui/icon-symbol";
import { useBounceAnimation } from "@/hooks/useBounceAnimation";
import { useUploadNutrition } from "@/features/nutrition/hooks/useNutrition";

const STEPS = [
  "Menganalisis gambar makanan si kecil...",
  "Mengidentifikasi bahan makanan dengan AI...",
  "Menghitung kalori dan komposisi makronutrisi...",
  "Menyimpan data log gizi ke akun...",
];

export const AnalysisScreen = () => {
  const [currentStep, setCurrentStep] = useState(0);
  const animatedIconStyle = useBounceAnimation();
  const { childId, imageUri } = useLocalSearchParams<{ childId: string; imageUri: string }>();
  const uploadNutrition = useUploadNutrition(childId ?? "");
  const mutationRef = useRef(false);

  useEffect(() => {
    // Start mutation only once
    if (!mutationRef.current && childId && imageUri) {
      mutationRef.current = true;

      const processImage = async () => {
        try {
          await uploadNutrition.mutateAsync({
            childId,
            photo: {
              uri: imageUri,
              name: "scan.jpg",
              type: "image/jpeg",
            }
          });
          setCurrentStep(STEPS.length);
          // Wait a bit before redirecting so user sees completion
          setTimeout(() => {
            router.replace("/(app)/(tabs)/scanner" as any);
          }, 800);
        } catch (error) {
          Alert.alert("Error", "Gagal menganalisis foto makanan.");
          router.replace("/(app)/(tabs)/scanner" as any);
        }
      };

      void processImage();
    }
  }, [childId, imageUri, uploadNutrition]);

  useEffect(() => {
    // Artificial step progression for UX
    if (uploadNutrition.isPending && currentStep < STEPS.length - 1) {
      const timer = setTimeout(() => {
        setCurrentStep((prev) => prev + 1);
      }, 1500); // Progress steps while waiting
      return () => clearTimeout(timer);
    }
  }, [currentStep, uploadNutrition.isPending]);

  return (
    <SafeAreaView className="flex-1 bg-background items-center justify-center px-6">
      <View className="items-center gap-6">
        {/* Animated Icon */}
        <View className="w-24 h-24 rounded-full bg-primary-light items-center justify-center shadow-lg border border-primary/20">
          <Animated.Text className="text-4xl" style={animatedIconStyle}>🥗</Animated.Text>
        </View>

        <View className="items-center gap-2">
          <Text className="font-extrabold text-xl text-primary text-center">Gemini AI Vision Analysis</Text>
          <Text className="text-xs text-outline text-center px-6 leading-4">
            Kami sedang memproses foto makanan dengan model kecerdasan buatan Gemini.
          </Text>
        </View>

        {/* Steps List */}
        <View className="w-full max-w-sm mt-8 gap-4 px-4">
          {STEPS.map((step, idx) => {
            const isCompleted = idx < currentStep;
            const isActive = idx === currentStep;

            return (
              <View key={idx} className="flex-row items-center gap-3 opacity-90">
                <View className="w-6 h-6 items-center justify-center">
                  {isCompleted ? (
                    <IconSymbol name="checkmark.circle.fill" size={18} color="#506444" />
                  ) : isActive ? (
                    <ActivityIndicator size="small" color="#3e646a" />
                  ) : (
                    <View className="w-2.5 h-2.5 rounded-full bg-surface-dim" />
                  )}
                </View>
                <Text
                  className={`text-xs flex-1 ${
                    isCompleted
                      ? "text-secondary font-bold line-through opacity-60"
                      : isActive
                      ? "text-primary font-bold"
                      : "text-outline font-medium"
                  }`}
                >
                  {step}
                </Text>
              </View>
            );
          })}
        </View>
      </View>
    </SafeAreaView>
  );
};
