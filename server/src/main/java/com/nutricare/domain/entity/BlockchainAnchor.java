package com.nutricare.domain.entity;

import com.nutricare.domain.enums.AnchorStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Entity BlockchainAnchor — tabel "blockchain_anchors"
 *
 * Menyimpan bukti bahwa data assessment telah di-anchor ke blockchain
 * (Polygon network) melalui smart contract GiziChainRegistry.
 *
 * Flow anchoring:
 * 1. Assessment selesai → hitung keccak256 hash dari data
 * 2. Kirim hash ke smart contract → dapat tx_hash
 * 3. Tunggu konfirmasi → simpan block_number
 * 4. anchor_status berubah: PENDING → CONFIRMED
 *
 * Jika saldo MATIC tidak cukup → status PENDING_GAS
 * Retry job akan coba lagi secara berkala.
 */
@Entity
@Table(name = "blockchain_anchors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainAnchor {

    @Id
    @Column(length = 30)
    private String id; // CUID

    // One-to-One ke Assessment
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false, unique = true)
    private Assessment assessment;

    // ── DATA BLOCKCHAIN ───────────────────────────────────────────────────────

    // keccak256 hash dari data assessment (66 karakter: "0x" + 64 hex)
    @Column(name = "record_hash", nullable = false, length = 66)
    private String recordHash;

    // Transaction hash di Polygon (66 karakter) — null sebelum tx terkirim
    @Column(name = "tx_hash", length = 66)
    private String txHash;

    // Block number saat tx dikonfirmasi — null sebelum confirmed
    @Column(name = "block_number")
    private Integer blockNumber;

    // Address smart contract GiziChainRegistry (42 karakter Ethereum address)
    @Column(name = "contract_address", nullable = false, length = 42)
    private String contractAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "anchor_status", nullable = false,
            columnDefinition = "anchor_status_enum")
    @Builder.Default
    private AnchorStatus anchorStatus = AnchorStatus.PENDING;

    @Column(name = "anchored_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMPTZ DEFAULT now()")
    @Builder.Default
    private OffsetDateTime anchoredAt = OffsetDateTime.now();

}
