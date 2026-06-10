package com.nutricare.dto.response.prediction;

import com.nutricare.domain.enums.PredictionStatus;
import com.nutricare.domain.enums.StuntStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class PredictionResponse {
    private String id;
    private String assessmentId;
    private String childId;
    private String childName;

    // Status
    private StuntStatus status;
    private PredictionStatus predictionStatus;
    private Short riskLevel;
    private String statusLabel;   // "Normal", "Berisiko Stunting", dll
    private String statusColor;   // "green", "yellow", "orange", "red"

    // Z-Score WHO
    private BigDecimal zscoreWa;  // Berat/Usia
    private BigDecimal zscoreHa;  // Tinggi/Usia
    private BigDecimal zscoreWh;  // Berat/Tinggi

    // Output Gemini
    private String summary;
    private List<String> recommendations;
    private LocalDate nextAssessmentDate;

    // Disclaimer wajib (dari CONTEXT.md)
    private String disclaimer;

    // Blockchain
    private BlockchainInfo blockchain;

    private OffsetDateTime createdAt;

    @Data
    @Builder
    public static class BlockchainInfo {
        private String anchorStatus;
        private String txHash;
        private String polygonscanUrl;
        private Boolean isVerified;
    }

    // Helper: label dan warna berdasarkan status
    public static String getLabelForStatus(StuntStatus status) {
        return switch (status) {
            case NORMAL -> "Normal";
            case AT_RISK -> "Berisiko Stunting";
            case STUNTED -> "Stunting";
            case SEVERELY_STUNTED -> "Stunting Berat";
        };
    }

    public static String getColorForStatus(StuntStatus status) {
        return switch (status) {
            case NORMAL -> "green";
            case AT_RISK -> "yellow";
            case STUNTED -> "orange";
            case SEVERELY_STUNTED -> "red";
        };
    }

    public static final String DISCLAIMER =
        "Hasil ini bersifat skrining awal dan bukan diagnosis medis. " +
        "Konsultasikan dengan dokter atau tenaga kesehatan.";
}
