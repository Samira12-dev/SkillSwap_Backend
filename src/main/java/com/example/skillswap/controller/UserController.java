package com.example.skillswap.controller;

import com.example.skillswap.dto.request.UpdateProfileRequestDTO;
import com.example.skillswap.dto.response.UserResponseDTO;
import com.example.skillswap.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{userId}")
    public UserResponseDTO getUserById(@PathVariable Long userId){
        return service.getUserById(userId);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(@PageableDefault(page = 0, size = 10) Pageable pageable){
        return ResponseEntity.ok(service.getAllUsers(pageable));
    }


    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{userId}")
    public UserResponseDTO updateProfile(@PathVariable Long userId, @Valid @RequestBody UpdateProfileRequestDTO dto, Authentication authentication){
        return service.updateProfile(userId,authentication.getName(),dto);
    }
}
