package com.nutricare.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    @NotBlank(message = "Prediction ID wajib diisi")
    private String predictionId;

    @NotBlank(message = "Pesan tidak boleh kosong")
    private String message;
}
