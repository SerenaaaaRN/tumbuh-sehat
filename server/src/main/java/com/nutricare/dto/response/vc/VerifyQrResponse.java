package com.nutricare.dto.response.vc;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Response verifikasi QR code Verifiable Credential.
 * Endpoint publik yang bisa diakses oleh faskes manapun tanpa autentikasi.
 */
@Data
@Builder
public class VerifyQrResponse {
    private Boolean valid;
    private String vcId;
    private String vcType;
    private String childAnonId;
    private String issuerName;
    private OffsetDateTime issuedAt;
    private OffsetDateTime expiresAt;
    private Boolean isRevoked;
    private String verificationMethod;
    private String ipfsCid;
}
