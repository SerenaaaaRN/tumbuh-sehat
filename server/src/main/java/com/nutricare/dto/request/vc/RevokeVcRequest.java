package com.nutricare.dto.request.vc;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request untuk mencabut (revoke) Verifiable Credential.
 * Hanya issuer dari VC tersebut yang bisa mencabut.
 */
@Data
public class RevokeVcRequest {

    @NotBlank(message = "VC ID wajib diisi")
    private String vcId;
}
