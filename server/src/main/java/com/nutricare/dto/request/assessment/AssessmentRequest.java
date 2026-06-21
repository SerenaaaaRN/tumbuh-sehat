package com.nutricare.dto.request.assessment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssessmentRequest {
    @NotBlank private String childId;

    @NotNull(message = "Berat badan wajib diisi")
    @Positive(message = "Berat badan harus positif")
    private BigDecimal weight;

    @NotNull(message = "Tinggi badan wajib diisi")
    @Positive(message = "Tinggi badan harus positif")
    private BigDecimal height;

    private BigDecimal headCircumference; // opsional

    @NotNull(message = "Status ASI eksklusif wajib diisi")
    private Boolean bfExclusive;

    private Short mpasiAge;  // opsional

    @NotNull(message = "Frekuensi makan wajib diisi")
    private Short mealFreq;

    private String illnessHistory; // opsional
}
