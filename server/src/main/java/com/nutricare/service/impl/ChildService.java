package com.nutricare.service.impl;

import com.nutricare.domain.entity.Child;
import com.nutricare.domain.entity.User;
import com.nutricare.dto.request.child.ChildRequest;
import com.nutricare.dto.response.child.ChildResponse;
import com.nutricare.exception.ForbiddenException;
import com.nutricare.exception.ResourceNotFoundException;
import com.nutricare.repository.ChildRepository;
import com.nutricare.repository.UserRepository;
import com.nutricare.util.CuidGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ChildService — Phase 2 (BE-201 sampai BE-204)
 */
@Service
@RequiredArgsConstructor
public class ChildService {

    private final ChildRepository childRepository;
    private final UserRepository userRepository;

    // ── BE-201: Get all children ──────────────────────────────────────────────

    public List<ChildResponse> getChildren(String userId) {
        return childRepository.findByUserId(userId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // ── BE-202: Create child ──────────────────────────────────────────────────

    @Transactional
    public ChildResponse createChild(ChildRequest request, String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));

        // Generate anon_id unik untuk blockchain
        String anonId;
        do { anonId = CuidGenerator.generate(); }
        while (childRepository.existsByAnonId(anonId));

        Child child = Child.builder()
            .id(CuidGenerator.generate())
            .user(user)
            .name(request.getName())
            .gender(request.getGender())
            .birthDate(request.getBirthDate())
            .anonId(anonId)
            .build();

        return mapToResponse(childRepository.save(child));
    }

    // ── BE-203: Get child by ID + ownership check ─────────────────────────────

    public ChildResponse getChild(String childId, String userId) {
        Child child = childRepository.findById(childId)
            .orElseThrow(() -> new ResourceNotFoundException("Anak tidak ditemukan"));

        // PARENT hanya bisa akses anaknya sendiri
        // MEDIC & ADMIN bisa akses semua (dihandle di controller via @PreAuthorize)
        if (!child.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Anda tidak memiliki akses ke data ini");
        }

        return mapToResponse(child);
    }

    // ── BE-204: Update child ──────────────────────────────────────────────────

    @Transactional
    public ChildResponse updateChild(String childId, ChildRequest request, String userId) {
        Child child = childRepository.findByIdAndUserId(childId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Anak tidak ditemukan atau bukan milik Anda"));

        child.setName(request.getName());
        child.setGender(request.getGender());
        child.setBirthDate(request.getBirthDate());

        return mapToResponse(childRepository.save(child));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ChildResponse mapToResponse(Child child) {
        int ageMonths = Period.between(child.getBirthDate(), LocalDate.now()).getMonths()
                      + Period.between(child.getBirthDate(), LocalDate.now()).getYears() * 12;

        return ChildResponse.builder()
            .id(child.getId())
            .name(child.getName())
            .gender(child.getGender())
            .birthDate(child.getBirthDate())
            .anonId(child.getAnonId())
            .ageMonths(ageMonths)
            .createdAt(child.getCreatedAt())
            .build();
    }
}
