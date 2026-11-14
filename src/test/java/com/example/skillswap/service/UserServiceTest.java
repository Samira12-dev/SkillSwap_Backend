package com.example.skillswap.service;

import com.example.skillswap.dto.response.UserResponseDTO;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.AccountStatus;
import com.example.skillswap.enums.Role;
import com.example.skillswap.mapper.UserMapper;
import com.example.skillswap.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserService service;

    @Test
    void publicLookupHidesPrivateAccountFields() {
        User target = user(2L, Role.USER);
        User requester = user(1L, Role.USER);

        UserResponseDTO response = sensitiveResponse();

        when(userRepo.findById(2L)).thenReturn(Optional.of(target));
        when(userRepo.findByEmail("viewer@example.com")).thenReturn(Optional.of(requester));
        when(mapper.toResponse(target)).thenReturn(response);

        UserResponseDTO result = service.getUserById(2L, "viewer@example.com");

        assertNull(result.getEmail());
        assertNull(result.getPhone());
        assertNull(result.getEmailVerified());
        assertNull(result.getPhoneVerified());
        assertNull(result.getRole());
        assertNull(result.getAccountStatus());
        assertEquals("Target", result.getFirstName());
    }

    @Test
    void selfLookupRetainsPrivateAccountFields() {
        User target = user(2L, Role.USER);

        UserResponseDTO response = sensitiveResponse();

        when(userRepo.findById(2L)).thenReturn(Optional.of(target));
        when(userRepo.findByEmail("target@example.com")).thenReturn(Optional.of(target));
        when(mapper.toResponse(target)).thenReturn(response);

        UserResponseDTO result = service.getUserById(2L, "target@example.com");

        assertEquals("target@example.com", result.getEmail());
        assertEquals("+123456789", result.getPhone());
        assertEquals(Role.USER, result.getRole());
    }

    @Test
    void adminLookupRetainsPrivateAccountFields() {
        User target = user(2L, Role.USER);
        User admin = user(1L, Role.ADMIN);

        UserResponseDTO response = sensitiveResponse();

        when(userRepo.findById(2L)).thenReturn(Optional.of(target));
        when(userRepo.findByEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(mapper.toResponse(target)).thenReturn(response);

        UserResponseDTO result = service.getUserById(2L, "admin@example.com");

        assertEquals("target@example.com", result.getEmail());
        assertEquals("+123456789", result.getPhone());
        assertEquals(Role.USER, result.getRole());
    }

    private User user(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail("target@example.com");
        user.setRole(role);
        user.setAccountStatus(AccountStatus.ACTIVE);
        return user;
    }

    private UserResponseDTO sensitiveResponse() {
        UserResponseDTO response = new UserResponseDTO();
        response.setFirstName("Target");
        response.setEmail("target@example.com");
        response.setPhone("+123456789");
        response.setEmailVerified(true);
        response.setPhoneVerified(true);
        response.setRole(Role.USER);
        response.setAccountStatus(AccountStatus.ACTIVE);
        return response;
    }
}
