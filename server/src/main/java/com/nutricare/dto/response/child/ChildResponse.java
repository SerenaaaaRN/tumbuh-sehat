package com.nutricare.dto.response.child;

import com.nutricare.domain.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
public class ChildResponse {
    private String id;
    private String name;
    private Gender gender;
    private LocalDate birthDate;
    private String anonId;
    private int ageMonths;    // dihitung di service
    private OffsetDateTime createdAt;
}
