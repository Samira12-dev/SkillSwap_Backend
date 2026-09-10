package com.example.skillswap.service;

import com.example.skillswap.dto.request.UpdateProfileRequestDTO;
import com.example.skillswap.dto.response.UserResponseDTO;
import com.example.skillswap.entity.User;
import com.example.skillswap.mapper.UserMapper;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final UserMapper mapper;


    @Transactional
    public UserResponseDTO getUserById(Long userId){
        User user=userRepo.findById(userId).orElseThrow(()->new RuntimeException("User not found"));
        return mapper.toResponse(user);
    }
    @Transactional
    public List<UserResponseDTO>getAllUsers(){
        return userRepo.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public UserResponseDTO updateProfile(Long userId, UpdateProfileRequestDTO dto){
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        mapper.updateUser(dto,user);
        User savedUser= userRepo.save(user);
        return mapper.toResponse(savedUser);
    }


}
