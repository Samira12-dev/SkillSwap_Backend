package com.example.skillswap.controller;

import com.example.skillswap.dto.request.UpdateProfileRequestDTO;
import com.example.skillswap.dto.response.UserResponseDTO;
import com.example.skillswap.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping("/{userId}")
    public UserResponseDTO getUserById(@PathVariable Long userId){
        return service.getUserById(userId);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        return ResponseEntity.ok(service.getAllUsers());
    }

    @PutMapping("/{userId}")
    public UserResponseDTO updateProfile(@PathVariable Long userId,@Valid @RequestBody UpdateProfileRequestDTO dto){
        return service.updateProfile(userId,dto);
    }
}
