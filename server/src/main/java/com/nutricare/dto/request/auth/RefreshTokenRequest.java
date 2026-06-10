package com.nutricare.dto.request.auth;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token wajib diisi")
    private String refreshToken;
}
