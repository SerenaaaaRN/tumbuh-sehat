package com.nutricare.dto.request.vc;

import com.nutricare.domain.enums.VcType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Request untuk menerbitkan Verifiable Credential baru.
 * Hanya MEDIC dan ADMIN yang bisa mengakses endpoint ini.
 */
@Data
public class IssueVcRequest {

    @NotBlank(message = "Child ID wajib diisi")
    private String childId;

    @NotNull(message = "Tipe VC wajib diisi")
    private VcType vcType;

    private OffsetDateTime expiresAt;
}
