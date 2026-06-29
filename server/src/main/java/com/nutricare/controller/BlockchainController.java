package com.nutricare.controller;

import com.nutricare.domain.entity.User;
import com.nutricare.domain.enums.Role;
import com.nutricare.dto.request.blockchain.AnchorRequest;
import com.nutricare.dto.response.blockchain.AnchorResponse;
import com.nutricare.dto.response.blockchain.VerifyResponse;
import com.nutricare.exception.ForbiddenException;
import com.nutricare.exception.ResourceNotFoundException;
import com.nutricare.repository.AssessmentRepository;
import com.nutricare.service.impl.BlockchainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BlockchainController — /api/blockchain/**
 *
 * BE-501: POST /api/blockchain/anchor — anchor hash assessment ke Polygon
 * BE-502: GET  /api/blockchain/verify/{assessmentId} — verifikasi integritas
 */
@RestController
@RequestMapping("/api/blockchain")
@RequiredArgsConstructor
public class BlockchainController {

    private final BlockchainService blockchainService;
    private final AssessmentRepository assessmentRepository;

    /**
     * POST /api/blockchain/anchor
     * Anchor hash data assessment ke blockchain Polygon.
     * PARENT hanya bisa anchor assessment milik sendiri.
     * ADMIN bisa anchor semua.
     */
    @PostMapping("/anchor")
    @PreAuthorize("hasRole('PARENT') or hasRole('ADMIN')")
    public ResponseEntity<AnchorResponse> anchorAssessment(
            @Valid @RequestBody AnchorRequest request,
            @AuthenticationPrincipal User user) {

        // PARENT: verifikasi kepemilikan assessment
        if (user.getRole() == Role.PARENT) {
            var assessment = assessmentRepository.findById(request.getAssessmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assessment tidak ditemukan"));
            if (!assessment.getChild().getUser().getId().equals(user.getId())) {
                throw new ForbiddenException("Anda tidak memiliki akses ke assessment ini");
            }
        }

        return ResponseEntity.ok(blockchainService.anchorAssessment(request.getAssessmentId()));
    }

    /**
     * GET /api/blockchain/verify/{assessmentId}
     * Verifikasi integritas data assessment di blockchain.
     * Endpoint publik (permitAll) untuk transparansi.
     */
    @GetMapping("/verify/{assessmentId}")
    public ResponseEntity<VerifyResponse> verifyAssessment(@PathVariable String assessmentId) {
        return ResponseEntity.ok(blockchainService.verifyAssessment(assessmentId));
    }
}
