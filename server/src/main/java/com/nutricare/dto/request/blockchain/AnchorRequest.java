package com.nutricare.dto.request.blockchain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request untuk meng-anchor hash assessment ke blockchain Polygon.
 * Dipanggil secara internal oleh server setelah prediksi selesai.
 */
@Data
public class AnchorRequest {

    @NotBlank(message = "Assessment ID wajib diisi")
    private String assessmentId;
}
