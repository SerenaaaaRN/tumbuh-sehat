package com.nutricare.dto.response.vc;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Response setelah Verifiable Credential berhasil diterbitkan.
 * Berisi informasi VC lengkap termasuk IPFS CID dan transaksi blockchain.
 */
@Data
@Builder
public class IssueVcResponse {
    private String id;
    private String childId;
    private String childAnonId;
    private String issuerId;
    private String issuerWallet;
    private String vcType;
    private String ipfsCid;
    private String txHash;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
    private String qrPayload;
}
