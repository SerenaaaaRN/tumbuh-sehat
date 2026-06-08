package com.nutricare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Entity NutritionLog — tabel "nutrition_logs"
 *
 * Menyimpan log makanan anak beserta hasil analisis Gemini Vision.
 * Foto disimpan di Supabase Storage, hanya URL yang disimpan di DB.
 *
 * food_detected: array makanan yang terdeteksi dari foto
 * gemini_raw: raw response Gemini (JSONB) untuk debugging
 */
@Entity
@Table(name = "nutrition_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionLog {

    @Id
    @Column(length = 30)
    private String id; // CUID

    // FK ke children
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    // ── DATA FOTO ──────────────────────────────────────────────────────────────

    // Public URL foto di Supabase Storage (foto tidak disimpan di DB)
    @Column(name = "photo_url", nullable = false, columnDefinition = "TEXT")
    private String photoUrl;

    // ── HASIL ANALISIS GEMINI VISION ──────────────────────────────────────────

    // Daftar makanan yang terdeteksi — TEXT[] di PostgreSQL
    @Column(name = "food_detected", columnDefinition = "TEXT[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> foodDetected;

    // Estimasi porsi (contoh: "1 piring sedang")
    @Column(name = "portion_estimate", length = 100)
    private String portionEstimate;

    // Kandungan gizi
    @Column(precision = 6, scale = 1)
    private BigDecimal calories;    // kkal

    @Column(precision = 5, scale = 1)
    private BigDecimal protein;     // gram

    @Column(precision = 5, scale = 1)
    private BigDecimal carbs;       // gram

    @Column(precision = 5, scale = 1)
    private BigDecimal fat;         // gram

    @Column(precision = 5, scale = 1)
    private BigDecimal fiber;       // gram

    // Catatan kecukupan gizi dari Gemini
    @Column(name = "adequacy_note", columnDefinition = "TEXT")
    private String adequacyNote;

    // Rekomendasi MPASI dari Gemini
    @Column(name = "mpasi_recommendation", columnDefinition = "TEXT")
    private String mpasiRecommendation;

    // Raw response Gemini (JSONB) — untuk debugging & audit
    @Column(name = "gemini_raw", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private String geminiRaw;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
