package com.nutricare.controller;

import com.nutricare.domain.entity.User;
import com.nutricare.domain.enums.Role;
import com.nutricare.dto.request.child.ChildRequest;
import com.nutricare.dto.response.child.ChildResponse;
import com.nutricare.exception.ForbiddenException;
import com.nutricare.repository.ChildRepository;
import com.nutricare.service.impl.ChildService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ChildController — /api/children/**
 * BE-201: GET  /api/children
 * BE-202: POST /api/children
 * BE-203: GET  /api/children/{childId}
 * BE-204: PUT  /api/children/{childId}
 */
@RestController
@RequestMapping("/api/children")
@RequiredArgsConstructor
public class ChildController {

    private final ChildService childService;
    private final ChildRepository childRepository;

    /**
     * GET /api/children
     * PARENT → hanya anaknya sendiri
     * MEDIC & ADMIN → semua anak
     */
    @GetMapping
    public ResponseEntity<List<ChildResponse>> getChildren(@AuthenticationPrincipal User user) {
        if (user.getRole() == Role.PARENT) {
            return ResponseEntity.ok(childService.getChildren(user.getId()));
        }
        // MEDIC & ADMIN — ambil semua anak
        List<ChildResponse> all = childRepository.findAll().stream()
            .map(c -> childService.getChild(c.getId(), c.getUser().getId()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(all);
    }

    /**
     * POST /api/children
     * Hanya PARENT dan ADMIN yang bisa tambah anak
     */
    @PostMapping
    @PreAuthorize("hasRole('PARENT') or hasRole('ADMIN')")
    public ResponseEntity<ChildResponse> createChild(
            @Valid @RequestBody ChildRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(childService.createChild(request, user.getId()));
    }

    /**
     * GET /api/children/{childId}
     * PARENT → ownership check di service
     * MEDIC & ADMIN → bebas akses
     */
    @GetMapping("/{childId}")
    public ResponseEntity<ChildResponse> getChild(
            @PathVariable String childId,
            @AuthenticationPrincipal User user) {

        if (user.getRole() == Role.PARENT) {
            return ResponseEntity.ok(childService.getChild(childId, user.getId()));
        }
        // MEDIC & ADMIN: akses tanpa ownership check
        return ResponseEntity.ok(childService.getChild(childId, childRepository.findById(childId)
            .orElseThrow().getUser().getId()));
    }

    /**
     * PUT /api/children/{childId}
     * Hanya pemilik (PARENT) atau ADMIN
     */
    @PutMapping("/{childId}")
    @PreAuthorize("hasRole('PARENT') or hasRole('ADMIN')")
    public ResponseEntity<ChildResponse> updateChild(
            @PathVariable String childId,
            @Valid @RequestBody ChildRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(childService.updateChild(childId, request, user.getId()));
    }
}
