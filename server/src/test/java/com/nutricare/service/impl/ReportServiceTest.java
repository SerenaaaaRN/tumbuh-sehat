package com.nutricare.service.impl;

import com.nutricare.TestDataFactory;
import com.nutricare.domain.entity.*;
import com.nutricare.exception.ForbiddenException;
import com.nutricare.exception.ResourceNotFoundException;
import com.nutricare.repository.AssessmentRepository;
import com.nutricare.repository.ChildRepository;
import com.nutricare.repository.PredictionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ChildRepository childRepository;
    @Mock private AssessmentRepository assessmentRepository;
    @Mock private PredictionRepository predictionRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(childRepository, assessmentRepository, predictionRepository);
    }

    @Test
    void generateChildReport_shouldReturnPdfBytes() {
        User parent = TestDataFactory.createParent();
        Child child = TestDataFactory.createChild(parent);
        Assessment assessment = TestDataFactory.createAssessment(child);
        Prediction prediction = TestDataFactory.createPrediction(assessment);

        when(childRepository.findById(child.getId())).thenReturn(Optional.of(child));
        when(assessmentRepository.findByChildIdAndDateRange(
            eq(child.getId()), any(), any())).thenReturn(List.of(assessment));
        when(predictionRepository.findByAssessmentId(assessment.getId())).thenReturn(Optional.of(prediction));

        byte[] pdfBytes = reportService.generateChildReport(
            child.getId(), parent.getId(),
            LocalDate.now().minusMonths(1), LocalDate.now());

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // PDF magic bytes
        assertArrayEquals(new byte[]{0x25, 0x50, 0x44, 0x46},
            new byte[]{pdfBytes[0], pdfBytes[1], pdfBytes[2], pdfBytes[3]});
    }

    @Test
    void generateChildReport_shouldReturnPdf_whenNoAssessments() {
        User parent = TestDataFactory.createParent();
        Child child = TestDataFactory.createChild(parent);

        when(childRepository.findById(child.getId())).thenReturn(Optional.of(child));
        when(assessmentRepository.findByChildIdAndDateRange(
            eq(child.getId()), any(), any())).thenReturn(List.of());

        byte[] pdfBytes = reportService.generateChildReport(
            child.getId(), parent.getId(),
            LocalDate.now().minusMonths(1), LocalDate.now());

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateChildReport_shouldThrow_whenChildNotFound() {
        when(childRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> reportService.generateChildReport("nonexistent", "user",
                LocalDate.now().minusMonths(1), LocalDate.now()));
    }

    @Test
    void generateChildReport_shouldThrow_whenNotOwner() {
        User parent = TestDataFactory.createParent();
        User other = TestDataFactory.createParent();
        Child child = TestDataFactory.createChild(parent);

        when(childRepository.findById(child.getId())).thenReturn(Optional.of(child));

        assertThrows(ForbiddenException.class,
            () -> reportService.generateChildReport(child.getId(), other.getId(),
                LocalDate.now().minusMonths(1), LocalDate.now()));
    }
}
