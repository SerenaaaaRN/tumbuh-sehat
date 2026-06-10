package com.nutricare.dto.request.child;

import com.nutricare.domain.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ChildRequest {
    @NotBlank(message = "Nama anak wajib diisi")
    private String name;

    @NotNull(message = "Jenis kelamin wajib diisi")
    private Gender gender;

    @NotNull(message = "Tanggal lahir wajib diisi")
    @Past(message = "Tanggal lahir harus di masa lalu")
    private LocalDate birthDate;
}
