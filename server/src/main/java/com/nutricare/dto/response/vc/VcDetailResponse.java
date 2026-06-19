package com.nutricare.dto.response.vc;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Detail Verifiable Credential dalam format W3C.
 * Data bersifat publik dan anonim (tanpa PII).
 */
@Data
@Builder
public class VcDetailResponse {
    private String id;
    private java.util.List<String> context;
    private java.util.List<String> type;
    private Map<String, Object> issuer;
    private OffsetDateTime issuanceDate;
    private OffsetDateTime expirationDate;
    private Map<String, Object> credentialSubject;
    private Boolean isRevoked;
    private String ipfsCid;
    private String txHash;
}
