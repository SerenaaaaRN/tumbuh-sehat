package com.nutricare.util;

import com.nutricare.domain.enums.Gender;
import com.nutricare.domain.enums.StuntStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ZScoreCalculatorTest {

    private ZScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ZScoreCalculator();
    }

    @Test
    void calculateHeightForAge_shouldReturnNormal() {
        // Anak laki 24 bulan, tinggi 89cm — normal (median ~91.4, SD ~2.6 -> z ≈ (89-91.4)/2.6 ≈ -0.92)
        BigDecimal zscore = calculator.calculateHeightForAge(89.0, 24, Gender.MALE);
        assertNotNull(zscore);
        assertTrue(zscore.doubleValue() >= -2.0, "Should be normal (>= -2 SD), got: " + zscore);
        assertEquals(StuntStatus.NORMAL, calculator.determineStuntStatus(zscore));
    }

    @Test
    void calculateHeightForAge_shouldReturnStunted() {
        // Anak laki 24 bulan, tinggi 75cm — stunted
        BigDecimal zscore = calculator.calculateHeightForAge(75.0, 24, Gender.MALE);
        assertNotNull(zscore);
        assertTrue(zscore.doubleValue() < -2.0, "Should be stunted (< -2 SD)");
    }

    @Test
    void calculateHeightForAge_shouldReturnSeverelyStunted() {
        // Anak laki 24 bulan, tinggi 65cm — severely stunted
        BigDecimal zscore = calculator.calculateHeightForAge(65.0, 24, Gender.MALE);
        assertNotNull(zscore);
        assertTrue(zscore.doubleValue() < -3.0, "Should be severely stunted (< -3 SD)");
    }

    @Test
    void calculateWeightForAge_shouldReturnValue() {
        BigDecimal zscore = calculator.calculateWeightForAge(10.5, 24, Gender.FEMALE);
        assertNotNull(zscore);
    }

    @Test
    void calculateWeightForHeight_shouldReturnValue() {
        BigDecimal zscore = calculator.calculateWeightForHeight(10.5, 85.0, Gender.MALE);
        assertNotNull(zscore);
    }

    @Test
    void determineStuntStatus_Normal() {
        assertEquals(StuntStatus.NORMAL, calculator.determineStuntStatus(BigDecimal.valueOf(-1.5)));
        assertEquals(StuntStatus.NORMAL, calculator.determineStuntStatus(BigDecimal.valueOf(0.0)));
        assertEquals(StuntStatus.NORMAL, calculator.determineStuntStatus(BigDecimal.valueOf(2.0)));
        assertEquals(StuntStatus.NORMAL, calculator.determineStuntStatus(BigDecimal.valueOf(-1.9)));
        assertEquals(StuntStatus.NORMAL, calculator.determineStuntStatus(BigDecimal.valueOf(-2.0))); // boundary
    }

    @Test
    void determineStuntStatus_AtRisk() {
        assertEquals(StuntStatus.AT_RISK, calculator.determineStuntStatus(BigDecimal.valueOf(-2.1)));
        assertEquals(StuntStatus.AT_RISK, calculator.determineStuntStatus(BigDecimal.valueOf(-2.3)));
        assertEquals(StuntStatus.AT_RISK, calculator.determineStuntStatus(BigDecimal.valueOf(-2.5))); // boundary
    }

    @Test
    void determineStuntStatus_Stunted() {
        assertEquals(StuntStatus.STUNTED, calculator.determineStuntStatus(BigDecimal.valueOf(-2.6)));
        assertEquals(StuntStatus.STUNTED, calculator.determineStuntStatus(BigDecimal.valueOf(-2.8)));
        assertEquals(StuntStatus.STUNTED, calculator.determineStuntStatus(BigDecimal.valueOf(-2.9))); // near severe
        assertEquals(StuntStatus.STUNTED, calculator.determineStuntStatus(BigDecimal.valueOf(-3.0))); // boundary
    }

    @Test
    void determineStuntStatus_SeverelyStunted() {
        assertEquals(StuntStatus.SEVERELY_STUNTED, calculator.determineStuntStatus(BigDecimal.valueOf(-3.1)));
        assertEquals(StuntStatus.SEVERELY_STUNTED, calculator.determineStuntStatus(BigDecimal.valueOf(-5.0)));
    }

    @Test
    void determineRiskLevel_shouldMapCorrectly() {
        assertEquals(1, calculator.determineRiskLevel(StuntStatus.NORMAL));
        assertEquals(2, calculator.determineRiskLevel(StuntStatus.AT_RISK));
        assertEquals(3, calculator.determineRiskLevel(StuntStatus.STUNTED));
        assertEquals(4, calculator.determineRiskLevel(StuntStatus.SEVERELY_STUNTED));
    }
}
