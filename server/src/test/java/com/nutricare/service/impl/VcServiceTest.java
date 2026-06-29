package com.nutricare.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutricare.TestDataFactory;
import com.nutricare.domain.entity.*;
import com.nutricare.domain.enums.VcType;
import com.nutricare.dto.request.vc.IssueVcRequest;
import com.nutricare.dto.request.vc.RevokeVcRequest;
import com.nutricare.exception.ForbiddenException;
import com.nutricare.exception.ResourceNotFoundException;
import com.nutricare.repository.ChildRepository;
import com.nutricare.repository.UserRepository;
import com.nutricare.repository.VerifiableCredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VcServiceTest {

    @Mock private VerifiableCredentialRepository vcRepository;
    @Mock private ChildRepository childRepository;
    @Mock private UserRepository userRepository;
    @Mock private IpfsService ipfsService;

    private ObjectMapper objectMapper;
    private VcService vcService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        vcService = new VcService(vcRepository, childRepository, userRepository, ipfsService, objectMapper);
        // Set simulation mode
        try {
            java.lang.reflect.Field field = VcService.class.getDeclaredField("simulation");
            field.setAccessible(true);
            field.set(vcService, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void issueVc_shouldSucceed() {
        User medic = TestDataFactory.createMedic();
        Child child = TestDataFactory.createChild(medic);

        IssueVcRequest request = new IssueVcRequest();
        request.setChildId(child.getId());
        request.setVcType(VcType.NUTRITION_STATUS);
        request.setExpiresAt(OffsetDateTime.now().plusYears(1));

        when(childRepository.findById(child.getId())).thenReturn(Optional.of(child));
        when(userRepository.findById(medic.getId())).thenReturn(Optional.of(medic));
        when(ipfsService.uploadJson(any())).thenReturn("QmTest123");
        when(vcRepository.save(any(VerifiableCredential.class))).thenAnswer(inv -> {
            VerifiableCredential vc = inv.getArgument(0);
            return VerifiableCredential.builder()
                .id(vc.getId())
                .child(vc.getChild())
                .issuer(vc.getIssuer())
                .vcType(vc.getVcType())
                .ipfsCid(vc.getIpfsCid())
                .txHash(vc.getTxHash())
                .isRevoked(false)
                .expiresAt(vc.getExpiresAt())
                .createdAt(OffsetDateTime.now())
                .build();
        });

        var response = vcService.issueVc(request, medic.getId());

        assertNotNull(response);
        assertEquals(child.getId(), response.getChildId());
        assertEquals(VcType.NUTRITION_STATUS.name(), response.getVcType());
        assertNotNull(response.getQrPayload());
        assertNotNull(response.getIpfsCid());
    }

    @Test
    void issueVc_shouldThrow_whenMedicNoWallet() {
        User medic = TestDataFactory.createUser(com.nutricare.domain.enums.Role.MEDIC, "medic2@test.com", "Medic2");
        medic.setWalletAddress(null); // no wallet
        Child child = TestDataFactory.createChild(medic);

        IssueVcRequest request = new IssueVcRequest();
        request.setChildId(child.getId());
        request.setVcType(VcType.NUTRITION_STATUS);

        when(childRepository.findById(child.getId())).thenReturn(Optional.of(child));
        when(userRepository.findById(medic.getId())).thenReturn(Optional.of(medic));

        assertThrows(ForbiddenException.class, () -> vcService.issueVc(request, medic.getId()));
    }

    @Test
    void getVc_shouldSucceed() {
        User medic = TestDataFactory.createMedic();
        Child child = TestDataFactory.createChild(medic);
        VerifiableCredential vc = TestDataFactory.createVc(child, medic);

        when(vcRepository.findById(vc.getId())).thenReturn(Optional.of(vc));

        var response = vcService.getVc(vc.getId());

        assertNotNull(response);
        assertFalse(response.getIsRevoked());
        assertEquals(vc.getId(), response.getId());
    }

    @Test
    void getVc_shouldThrow_whenNotFound() {
        when(vcRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vcService.getVc("nonexistent"));
    }

    @Test
    void revokeVc_shouldSucceed() {
        User medic = TestDataFactory.createMedic();
        Child child = TestDataFactory.createChild(medic);
        VerifiableCredential vc = TestDataFactory.createVc(child, medic);

        RevokeVcRequest request = new RevokeVcRequest();
        request.setVcId(vc.getId());

        when(vcRepository.findById(vc.getId())).thenReturn(Optional.of(vc));

        var response = vcService.revokeVc(request, medic.getId());

        assertNotNull(response);
        assertTrue(response.getIsRevoked());
    }

    @Test
    void revokeVc_shouldThrow_whenNotIssuer() {
        User medic = TestDataFactory.createMedic();
        User otherMedic = TestDataFactory.createUser(com.nutricare.domain.enums.Role.MEDIC, "other@test.com", "Lain");
        otherMedic.setWalletAddress("0x" + "f".repeat(40));
        Child child = TestDataFactory.createChild(medic);
        VerifiableCredential vc = TestDataFactory.createVc(child, medic);

        RevokeVcRequest request = new RevokeVcRequest();
        request.setVcId(vc.getId());

        when(vcRepository.findById(vc.getId())).thenReturn(Optional.of(vc));

        assertThrows(ForbiddenException.class, () -> vcService.revokeVc(request, otherMedic.getId()));
    }

    @Test
    void verifyQr_shouldSucceed() throws Exception {
        User medic = TestDataFactory.createMedic();
        Child child = TestDataFactory.createChild(medic);
        VerifiableCredential vc = TestDataFactory.createVc(child, medic);

        String qrPayload = java.util.Base64.getEncoder()
            .encodeToString(("{\"vcId\":\"" + vc.getId() + "\"}").getBytes());

        when(vcRepository.findById(vc.getId())).thenReturn(Optional.of(vc));

        var response = vcService.verifyQr(qrPayload);

        assertNotNull(response);
        assertTrue(response.getValid());
        assertEquals(vc.getId(), response.getVcId());
    }

    @Test
    void verifyQr_shouldThrow_whenInvalidPayload() {
        String invalidPayload = java.util.Base64.getEncoder()
            .encodeToString("invalid-json".getBytes());

        assertThrows(ResourceNotFoundException.class, () -> vcService.verifyQr(invalidPayload));
    }
}
