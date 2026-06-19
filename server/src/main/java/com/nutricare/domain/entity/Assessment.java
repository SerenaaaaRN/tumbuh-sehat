package com.nutricare.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entity Assessment — tabel "assessments"
 *
 * PENTING: Assessment bersifat IMMUTABLE (append-only).
 * Setelah data diinsert, tidak ada yang boleh diupdate.
 * Koreksi dilakukan dengan membuat assessment baru.
 *
 * Tidak ada kolom updated_at karena memang tidak boleh diubah.
 *
 * Relasi:
 * - Many-to-One ke Child (banyak assessment → 1 anak)
 * - One-to-One ke Prediction (1 assessment → 1 prediksi AI)
 * - One-to-One ke BlockchainAnchor (1 assessment → 1 anchor)
 */
@Entity
@Table(name = "assessments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assessment {

    @Id
    @Column(length = 30)
    private String id; // CUID

    // FK ke children
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    // ── DATA ANTROPOMETRI ──────────────────────────────────────────────────────

    // Berat badan (kg) — min 0.5, max 50
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    // Tinggi badan (cm) — min 30, max 130
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal height;

    // Lingkar kepala (cm) — opsional, min 20, max 60
    @Column(name = "head_circumference", precision = 4, scale = 1)
    private BigDecimal headCircumference;

    // ── DATA RIWAYAT MAKAN & KESEHATAN ─────────────────────────────────────────

    // ASI eksklusif 6 bulan pertama?
    @Column(name = "bf_exclusive", nullable = false)
    private Boolean bfExclusive;

    // Usia mulai MPASI (bulan) — opsional
    @Column(name = "mpasi_age")
    private Short mpasiAge;

    // Frekuensi makan per hari
    @Column(name = "meal_freq", nullable = false)
    private Short mealFreq;

    // Riwayat penyakit (teks bebas) — opsional
    @Column(name = "illness_history", columnDefinition = "TEXT")
    private String illnessHistory;

    // Tidak ada updated_at — assessment IMMUTABLE setelah insert
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // ── Relasi ────────────────────────────────────────────────────────────────

    // 1 assessment → 1 prediksi AI (dibuat setelah assessment)
    @OneToOne(mappedBy = "assessment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Prediction prediction;

    // 1 assessment → 1 blockchain anchor (dibuat setelah prediksi selesai)
    @OneToOne(mappedBy = "assessment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BlockchainAnchor blockchainAnchor;
}
