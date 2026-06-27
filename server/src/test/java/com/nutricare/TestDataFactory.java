package com.nutricare;

import com.nutricare.domain.entity.*;
import com.nutricare.domain.enums.*;
import com.nutricare.util.CuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Factory untuk membuat entity test yang konsisten di semua test.
 */
public class TestDataFactory {

    // ── User ───────────────────────────────────────────────────────────────────

    public static User createParent() {
        return createUser(Role.PARENT, "parent@test.com", "Orang Tua");
    }

    public static User createMedic() {
        User user = createUser(Role.MEDIC, "medic@test.com", "Tenaga Kesehatan");
        user.setWalletAddress("0x" + "1".repeat(40));
        return user;
    }

    public static User createAdmin() {
        User user = createUser(Role.ADMIN, "admin@test.com", "Administrator");
        user.setWalletAddress("0x" + "2".repeat(40));
        return user;
    }

    public static User createUser(Role role, String email, String name) {
        return User.builder()
            .id(CuidGenerator.generate())
            .email(email)
            .passwordHash("$2a$10$hash")
            .name(name)
            .role(role)
            .isActive(true)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();
    }

    // ── Child ──────────────────────────────────────────────────────────────────

    public static Child createChild(User parent) {
        return Child.builder()
            .id(CuidGenerator.generate())
            .user(parent)
            .name("Anak Sehat")
            .gender(Gender.MALE)
            .birthDate(LocalDate.now().minusMonths(24))
            .anonId(CuidGenerator.generate())
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();
    }

    // ── Assessment ─────────────────────────────────────────────────────────────

    public static Assessment createAssessment(Child child) {
        return Assessment.builder()
            .id(CuidGenerator.generate())
            .child(child)
            .weight(BigDecimal.valueOf(10.5))
            .height(BigDecimal.valueOf(85.0))
            .headCircumference(BigDecimal.valueOf(48.0))
            .bfExclusive(true)
            .mpasiAge((short) 6)
            .mealFreq((short) 3)
            .illnessHistory("Tidak ada")
            .createdAt(OffsetDateTime.now())
            .build();
    }

    // ── Prediction ─────────────────────────────────────────────────────────────

    public static Prediction createPrediction(Assessment assessment) {
        return Prediction.builder()
            .id(CuidGenerator.generate())
            .assessment(assessment)
            .status(StuntStatus.NORMAL)
            .predictionStatus(PredictionStatus.COMPLETED)
            .zscoreHa(BigDecimal.valueOf(-1.2))
            .zscoreWa(BigDecimal.valueOf(-0.8))
            .zscoreWh(BigDecimal.valueOf(-0.5))
            .riskLevel((short) 1)
            .summary("Anak dalam kondisi gizi baik")
            .recommendations(java.util.List.of("Lanjutkan ASI", "Tambah variasi MPASI"))
            .nextAssessmentDate(LocalDate.now().plusMonths(3))
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();
    }

    // ── BlockhainAnchor ────────────────────────────────────────────────────────

    public static BlockchainAnchor createAnchor(Assessment assessment) {
        return BlockchainAnchor.builder()
            .id(CuidGenerator.generate())
            .assessment(assessment)
            .recordHash("0x" + "a".repeat(64))
            .txHash("0x" + "b".repeat(64))
            .blockNumber(12345678)
            .contractAddress("0x" + "c".repeat(40))
            .anchorStatus(AnchorStatus.CONFIRMED)
            .anchoredAt(OffsetDateTime.now())
            .build();
    }

    // ── NutritionLog ───────────────────────────────────────────────────────────

    public static NutritionLog createNutritionLog(Child child) {
        NutritionLog log = new NutritionLog();
        log.setId(CuidGenerator.generate());
        log.setChild(child);
        log.setPhotoUrl("https://storage.supabase.co/food-photos/test.jpg");
        log.setFoodDetected(java.util.List.of("Nasi", "Sayur bayam", "Tempe"));
        log.setCalories(BigDecimal.valueOf(350));
        log.setProtein(BigDecimal.valueOf(12.5));
        log.setCarbs(BigDecimal.valueOf(45.0));
        log.setFat(BigDecimal.valueOf(8.0));
        log.setFiber(BigDecimal.valueOf(3.5));
        log.setAdequacyNote("Porsi cukup untuk usia anak");
        log.setGeminiRaw("{\"foodDetected\":[\"Nasi\"]}");
        log.setCreatedAt(OffsetDateTime.now());
        return log;
    }

    // ── ChatSession ────────────────────────────────────────────────────────────

    public static ChatSession createChatSession(Prediction prediction) {
        ChatSession session = new ChatSession();
        session.setId(CuidGenerator.generate());
        session.setPrediction(prediction);
        session.setMessages("[]");
        session.setUpdatedAt(OffsetDateTime.now());
        return session;
    }

    // ── VerifiableCredential ───────────────────────────────────────────────────

    public static VerifiableCredential createVc(Child child, User issuer) {
        return VerifiableCredential.builder()
            .id(CuidGenerator.generate())
            .child(child)
            .issuer(issuer)
            .vcType(VcType.NUTRITION_STATUS)
            .ipfsCid("QmTest123")
            .txHash("0x" + "d".repeat(64))
            .isRevoked(false)
            .expiresAt(OffsetDateTime.now().plusYears(1))
            .createdAt(OffsetDateTime.now())
            .build();
    }

    // ── RefreshToken ───────────────────────────────────────────────────────────

    public static RefreshToken createRefreshToken(User user) {
        return RefreshToken.builder()
            .id(CuidGenerator.generate())
            .user(user)
            .tokenHash("a".repeat(64))
            .expiresAt(OffsetDateTime.now().plusDays(7))
            .revoked(false)
            .createdAt(OffsetDateTime.now())
            .build();
    }
}
