package com.nutricare.service.impl;

import com.nutricare.TestDataFactory;
import com.nutricare.domain.entity.Child;
import com.nutricare.domain.entity.User;
import com.nutricare.dto.request.child.ChildRequest;
import com.nutricare.exception.ForbiddenException;
import com.nutricare.exception.ResourceNotFoundException;
import com.nutricare.repository.ChildRepository;
import com.nutricare.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChildServiceTest {

    @Mock private ChildRepository childRepository;
    @Mock private UserRepository userRepository;

    private ChildService childService;

    @BeforeEach
    void setUp() {
        childService = new ChildService(childRepository, userRepository);
    }

    @Test
    void getChildren_shouldReturnList() {
        User parent = TestDataFactory.createParent();
        Child child = TestDataFactory.createChild(parent);
        when(childRepository.findByUserId(parent.getId())).thenReturn(List.of(child));

        var result = childService.getChildren(parent.getId());

        assertEquals(1, result.size());
        assertEquals(child.getName(), result.get(0).getName());
    }

    @Test
    void createChild_shouldSucceed() {
        User parent = TestDataFactory.createParent();
        ChildRequest request = new ChildRequest();
        request.setName("Ani");
        request.setGender(com.nutricare.domain.enums.Gender.FEMALE);
        request.setBirthDate(LocalDate.of(2024, 1, 1));

        when(userRepository.findById(parent.getId())).thenReturn(Optional.of(parent));
        when(childRepository.existsByAnonId(anyString())).thenReturn(false);
        when(childRepository.save(any(Child.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = childService.createChild(request, parent.getId());

        assertNotNull(response);
        assertEquals("Ani", response.getName());
        assertEquals(com.nutricare.domain.enums.Gender.FEMALE, response.getGender());
    }

    @Test
    void getChild_shouldSucceed_forOwner() {
        User parent = TestDataFactory.createParent();
        Child child = TestDataFactory.createChild(parent);

        when(childRepository.findById(child.getId())).thenReturn(Optional.of(child));

        var response = childService.getChild(child.getId(), parent.getId());

        assertNotNull(response);
        assertEquals(child.getName(), response.getName());
    }

    @Test
    void getChild_shouldThrow_whenNotOwner() {
        User parent = TestDataFactory.createParent();
        User other = TestDataFactory.createUser(com.nutricare.domain.enums.Role.PARENT, "other@test.com", "Lain");
        Child child = TestDataFactory.createChild(parent);

        when(childRepository.findById(child.getId())).thenReturn(Optional.of(child));

        assertThrows(ForbiddenException.class, () -> childService.getChild(child.getId(), other.getId()));
    }

    @Test
    void getChild_shouldThrow_whenNotFound() {
        when(childRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> childService.getChild("nonexistent", "userid"));
    }

    @Test
    void updateChild_shouldSucceed() {
        User parent = TestDataFactory.createParent();
        Child child = TestDataFactory.createChild(parent);

        ChildRequest request = new ChildRequest();
        request.setName("Nama Baru");
        request.setGender(com.nutricare.domain.enums.Gender.MALE);
        request.setBirthDate(LocalDate.of(2023, 6, 15));

        when(childRepository.findByIdAndUserId(child.getId(), parent.getId())).thenReturn(Optional.of(child));
        when(childRepository.save(any(Child.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = childService.updateChild(child.getId(), request, parent.getId());

        assertEquals("Nama Baru", response.getName());
        assertEquals(com.nutricare.domain.enums.Gender.MALE, response.getGender());
    }

    @Test
    void updateChild_shouldThrow_whenNotFound() {
        ChildRequest request = new ChildRequest();
        request.setName("Nama");
        request.setGender(com.nutricare.domain.enums.Gender.MALE);
        request.setBirthDate(LocalDate.of(2023, 6, 15));

        when(childRepository.findByIdAndUserId(anyString(), anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> childService.updateChild("x", request, "y"));
    }
}
