package com.nutricare.dto.response.blockchain;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Response setelah data assessment berhasil di-anchor ke blockchain Polygon.
 */
@Data
@Builder
public class AnchorResponse {
    private String id;
    private String assessmentId;
    private String recordHash;
    private String txHash;
    private Integer blockNumber;
    private String contractAddress;
    private String anchorStatus;
    private OffsetDateTime anchoredAt;
}
