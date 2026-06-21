package com.nutricare.domain.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Entity RefreshToken — tabel "refresh_tokens"
 *
 * Digunakan untuk mekanisme refresh JWT token tanpa login ulang.
 *
 * Flow:
 * 1. User login → dapat access token (15 menit) + refresh token (7 hari)
 * 2. Access token expired → kirim refresh token → dapat access token baru
 * 3. Logout → refresh token di-revoke (revoked = true)
 *
 * token_hash: hash dari refresh token yang asli.
 * Token asli TIDAK disimpan di DB, hanya hash nya — lebih aman.
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @Column(length = 30)
    private String id; // CUID

    // FK ke users
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Hash dari refresh token (bukan token aslinya)
    @Column(name = "token_hash", unique = true, nullable = false, length = 255)
    private String tokenHash;

    // Waktu kadaluarsa (7 hari dari waktu pembuatan)
    @Column(name = "expires_at", nullable = false,
            columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime expiresAt;

    // True jika token sudah direvoke (logout / sudah dipakai refresh)
    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // ── Helper method ─────────────────────────────────────────────────────────

    // Cek apakah token sudah tidak valid
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}
