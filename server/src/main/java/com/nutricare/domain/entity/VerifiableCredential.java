package com.nutricare.domain.entity;

import com.nutricare.domain.enums.VcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Entity VerifiableCredential — tabel "verifiable_credentials"
 *
 * Verifiable Credential (VC) adalah dokumen digital terverifikasi
 * yang diterbitkan oleh tenaga kesehatan (MEDIC) untuk anak.
 *
 * Contoh VC:
 * - IMMUNIZATION_COMPLETE: bukti imunisasi lengkap
 * - NUTRITION_STATUS: status gizi anak
 * - GROWTH_MILESTONE: capaian tumbuh kembang
 *
 * Flow penerbitan VC:
 * 1. MEDIC buat dokumen VC
 * 2. Upload dokumen ke IPFS via Pinata → dapat ipfs_cid
 * 3. Catat CID di smart contract VCRegistry → dapat tx_hash
 * 4. Simpan ke database
 *
 * Menggunakan anon_id anak (bukan nama asli) di dokumen on-chain
 * untuk menjaga privasi data anak.
 */
@Entity
@Table(name = "verifiable_credentials")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifiableCredential {

    @Id
    @Column(length = 30)
    private String id; // CUID

    // FK ke children
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    // FK ke users (MEDIC yang menerbitkan)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_id", nullable = false)
    private User issuer;

    // ── DATA CREDENTIAL ───────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "vc_type", nullable = false)
    private VcType vcType;

    // Content Identifier dokumen VC di IPFS (via Pinata)
    @Column(name = "ipfs_cid", nullable = false, length = 100)
    private String ipfsCid;

    // Transaction hash pencatatan CID di VCRegistry contract
    @Column(name = "tx_hash", nullable = false, length = 66)
    private String txHash;

    // ── STATUS REVOKASI ───────────────────────────────────────────────────────

    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private Boolean isRevoked = false;

    // TX hash saat VC direvoke on-chain — null jika belum direvoke
    @Column(name = "revoke_tx_hash", length = 66)
    private String revokeTxHash;

    // Tanggal kadaluarsa VC — null jika tidak ada batas waktu
    @Column(name = "expires_at", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // ── Helper ────────────────────────────────────────────────────────────────

    public boolean isValid() {
        if (isRevoked) return false;
        if (expiresAt != null && OffsetDateTime.now().isAfter(expiresAt)) return false;
        return true;
    }
}
