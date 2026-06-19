package com.nutricare.dto.response.blockchain;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Response verifikasi integritas data assessment di blockchain.
 * Membandingkan hash on-chain dengan hash yang tersimpan di database.
 */
@Data
@Builder
public class VerifyResponse {
    private String assessmentId;
    private Boolean isValid;
    private String recordHash;
    private OffsetDateTime anchoredAt;
    private String txHash;
    private Integer blockNumber;
    private String explorerUrl;
}
