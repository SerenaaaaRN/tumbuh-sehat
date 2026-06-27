package com.nutricare.service.impl;

import com.nutricare.TestDataFactory;
import com.nutricare.domain.entity.*;
import com.nutricare.domain.enums.AnchorStatus;
import com.nutricare.dto.response.blockchain.AnchorResponse;
import com.nutricare.exception.ResourceNotFoundException;
import com.nutricare.repository.AssessmentRepository;
import com.nutricare.repository.BlockchainAnchorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockchainServiceTest {

    @Mock private BlockchainAnchorRepository blockchainAnchorRepository;
    @Mock private AssessmentRepository assessmentRepository;

    private BlockchainService blockchainService;

    @BeforeEach
    void setUp() {
        blockchainService = new BlockchainService(blockchainAnchorRepository, assessmentRepository);
        setField(blockchainService, "simulation", true);
        setField(blockchainService, "registryContract", "0x" + "c".repeat(40));
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void anchorAssessment_shouldSucceed_inSimulationMode() {
        User parent = TestDataFactory.createParent();
        Child child = TestDataFactory.createChild(parent);
        Assessment assessment = TestDataFactory.createAssessment(child);

        when(assessmentRepository.findById(assessment.getId())).thenReturn(Optional.of(assessment));
        when(blockchainAnchorRepository.save(any(BlockchainAnchor.class))).thenAnswer(inv -> inv.getArgument(0));

        AnchorResponse response = blockchainService.anchorAssessment(assessment.getId());

        assertNotNull(response);
        assertEquals(assessment.getId(), response.getAssessmentId());
        assertTrue(response.getTxHash().startsWith("0x"));
        assertEquals(AnchorStatus.CONFIRMED.name(), response.getAnchorStatus());
        assertNotNull(response.getRecordHash());
        assertTrue(response.getRecordHash().startsWith("0x"));
    }

    @Test
    void anchorAssessment_shouldThrow_whenAssessmentNotFound() {
        when(assessmentRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> blockchainService.anchorAssessment("nonexistent"));
    }

    @Test
    void verifyAssessment_shouldSucceed() throws Exception {
        User parent = TestDataFactory.createParent();
        Child child = TestDataFactory.createChild(parent);
        Assessment assessment = TestDataFactory.createAssessment(child);
        Prediction prediction = TestDataFactory.createPrediction(assessment);

        // Hitung hash real, bukan dummy
        java.lang.reflect.Method calcMethod = BlockchainService.class.getDeclaredMethod("calculateRecordHash", Assessment.class);
        calcMethod.setAccessible(true);
        String realHash = (String) calcMethod.invoke(blockchainService, assessment);

        var anchor = TestDataFactory.createAnchor(assessment);
        anchor.setRecordHash(realHash);

        when(assessmentRepository.findById(assessment.getId())).thenReturn(Optional.of(assessment));
        when(blockchainAnchorRepository.findByAssessmentId(assessment.getId())).thenReturn(Optional.of(anchor));

        var response = blockchainService.verifyAssessment(assessment.getId());

        assertNotNull(response);
        assertTrue(response.getIsValid());
        assertEquals(assessment.getId(), response.getAssessmentId());
        assertNotNull(response.getExplorerUrl());
    }

    @Test
    void verifyAssessment_shouldReturnInvalid_whenHashMismatch() {
        User parent = TestDataFactory.createParent();
        Child child = TestDataFactory.createChild(parent);
        Assessment assessment = TestDataFactory.createAssessment(child);
        var anchor = TestDataFactory.createAnchor(assessment);
        anchor.setRecordHash("0x" + "d".repeat(64)); // different hash

        when(assessmentRepository.findById(assessment.getId())).thenReturn(Optional.of(assessment));
        when(blockchainAnchorRepository.findByAssessmentId(assessment.getId())).thenReturn(Optional.of(anchor));

        var response = blockchainService.verifyAssessment(assessment.getId());

        assertNotNull(response);
        assertFalse(response.getIsValid());
    }

    @Test
    void verifyAssessment_shouldThrow_whenNotFound() {
        when(assessmentRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> blockchainService.verifyAssessment("nonexistent"));
    }
}
