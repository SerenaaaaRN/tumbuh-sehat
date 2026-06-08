package com.nutricare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Entity Child — tabel "children"
 *
 * anon_id adalah ID publik anak yang dipakai di dokumen on-chain (VC/blockchain)
 * untuk menggantikan nama asli — menjaga privasi data anak.
 *
 * Data anak TIDAK PERNAH dihapus (no soft delete),
 * hanya user (orang tua) yang bisa di-deactivate.
 */
@Entity
@Table(name = "children")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Child {

    @Id
    @Column(length = 30)
    private String id; // CUID

    // FK ke users — banyak anak dimiliki 1 user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "gender_enum")
    private Gender gender;

    // ID anonim untuk keperluan on-chain (menggantikan nama/PII)
    @Column(name = "anon_id", unique = true, nullable = false, length = 30)
    private String anonId; // CUID baru, di-generate saat create child

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    // ── Relasi ────────────────────────────────────────────────────────────────

    // 1 anak → banyak assessment (riwayat pengukuran)
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Assessment> assessments;

    // 1 anak → banyak log makanan
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NutritionLog> nutritionLogs;

    // 1 anak → banyak verifiable credential
    @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VerifiableCredential> verifiableCredentials;

    // ── Enum Gender ───────────────────────────────────────────────────────────

    public enum Gender {
        MALE, FEMALE
    }
}
