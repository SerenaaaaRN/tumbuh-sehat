package com.nutricare.domain.entity;

import com.nutricare.domain.enums.PredictionStatus;
import com.nutricare.domain.enums.StuntStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Entity Prediction — tabel "predictions"
 *
 * Menyimpan hasil prediksi AI (Gemini) berdasarkan data assessment.
 * Relasi One-to-One dengan Assessment.
 *
 * prediction_status melacak proses AI:
 * PENDING → sedang diproses Gemini
 * COMPLETED → prediksi berhasil
 * FAILED → Gemini gagal setelah retry
 *
 * gemini_raw menyimpan raw response Gemini dalam format JSONB
 * untuk keperluan debugging dan audit.
 */
@Entity
@Table(name = "predictions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prediction {

    @Id
    @Column(length = 30)
    private String id; // CUID

    // One-to-One ke Assessment
    // @JoinColumn: nama FK di tabel predictions
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false, unique = true)
    private Assessment assessment;

    // ── HASIL PREDIKSI ────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false)
    private StuntStatus status;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "prediction_status", nullable = false)
    @Builder.Default
    private PredictionStatus predictionStatus = PredictionStatus.PENDING;

    // ── Z-SCORE (Standar WHO) ──────────────────────────────────────────────────

    // Z-Score Berat/Usia (Weight for Age)
    @Column(name = "zscore_wa", precision = 5, scale = 2)
    private BigDecimal zscoreWa;

    // Z-Score Tinggi/Usia (Height for Age) — indikator utama stunting
    @Column(name = "zscore_ha", precision = 5, scale = 2)
    private BigDecimal zscoreHa;

    // Z-Score Berat/Tinggi (Weight for Height)
    @Column(name = "zscore_wh", precision = 5, scale = 2)
    private BigDecimal zscoreWh;

    // Level risiko 1-4 (1=normal, 2=at_risk, 3=stunted, 4=severely_stunted)
    @Column(name = "risk_level")
    private Short riskLevel;

    // ── OUTPUT GEMINI ──────────────────────────────────────────────────────────

    // Ringkasan penjelasan dari Gemini
    @Column(columnDefinition = "TEXT")
    private String summary;

    // Array rekomendasi dari Gemini — disimpan sebagai TEXT[] di PostgreSQL
    @Column(columnDefinition = "TEXT[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> recommendations;

    // Tanggal assessment berikutnya yang disarankan AI
    @Column(name = "next_assessment_date")
    private LocalDate nextAssessmentDate;

    // Raw response Gemini (JSONB) — untuk debugging & audit
    @Column(name = "gemini_raw", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String geminiRaw;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // ── Relasi ────────────────────────────────────────────────────────────────

    // 1 prediksi → 1 sesi chat
    @OneToOne(mappedBy = "prediction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ChatSession chatSession;

}
