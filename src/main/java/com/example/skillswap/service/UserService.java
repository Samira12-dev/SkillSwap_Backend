package com.example.skillswap.service;

import com.example.skillswap.dto.request.UpdateProfileRequestDTO;
import com.example.skillswap.dto.response.UserResponseDTO;
import com.example.skillswap.entity.User;
import com.example.skillswap.mapper.UserMapper;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
    public Page<UserResponseDTO> getAllUsers(Pageable pageable){
        return userRepo.findAll(pageable).map(mapper::toResponse);
    }

    @Transactional
    public UserResponseDTO updateProfile(Long userId,String email, UpdateProfileRequestDTO dto){
        User userNow = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        if(!userNow.getId().equals(userId)){
            throw  new RuntimeException("you can only update your profile");
        }
        User user =userRepo.findById(userId).orElseThrow(()->new RuntimeException("User not found"));
        mapper.updateUser(dto,user);
        User savedUser= userRepo.save(user);
        return mapper.toResponse(savedUser);
    }


}
