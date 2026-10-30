package com.example.skillswap.service;

import com.example.skillswap.dto.request.UpdateProfileRequestDTO;
import com.example.skillswap.dto.response.UserResponseDTO;
import com.example.skillswap.entity.Review;
import com.example.skillswap.entity.User;
import com.example.skillswap.enums.Role;
import com.example.skillswap.mapper.UserMapper;
import com.example.skillswap.repository.NotificationRepo;
import com.example.skillswap.repository.ReviewRepo;
import com.example.skillswap.repository.SkillDetailsRepo;
import com.example.skillswap.repository.SwapRequestRepo;
import com.example.skillswap.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final UserMapper mapper;
    private final SwapRequestRepo swapRequestRepo;
    private final ReviewRepo reviewRepo;
    private final NotificationRepo notificationRepo;
    private final SkillDetailsRepo skillDetailsRepo;
    private final ReviewService reviewService;
    private final FileStorageService fileStorageService;


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

    @Transactional
    public UserResponseDTO uploadProfilePhoto(Long userId, String email, MultipartFile file) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getId().equals(userId)) {
            throw new AccessDeniedException("You can only update your own profile photo");
        }

        String previousPhoto = user.getPhoto();
        String newPhoto = fileStorageService.storeProfilePhoto(file);

        try {
            user.setPhoto(newPhoto);
            User savedUser = userRepo.save(user);

            if (previousPhoto != null && !previousPhoto.equals(newPhoto)) {
                fileStorageService.deleteManagedPhoto(previousPhoto);
            }

            return mapper.toResponse(savedUser);
        } catch (RuntimeException ex) {
            fileStorageService.deleteManagedPhoto(newPhoto);
            throw ex;
        }
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("You cannot delete an admin user");
        }

        List<Long> affectedRevieweeIds = new ArrayList<>();
        for (Review review : reviewRepo.findByReviewerId(userId, Pageable.unpaged()).getContent()) {
            Long id = review.getReviewee().getId();
            if (!affectedRevieweeIds.contains(id)) {
                affectedRevieweeIds.add(id);
            }
        }

        reviewRepo.deleteAll(reviewRepo.findByReviewerId(userId, Pageable.unpaged()).getContent());
        reviewRepo.deleteAll(reviewRepo.findByRevieweeId(userId, Pageable.unpaged()).getContent());
        notificationRepo.deleteAll(notificationRepo.findByUserId(userId, Pageable.unpaged()).getContent());
        swapRequestRepo.deleteAll(swapRequestRepo.findBySenderId(userId, Pageable.unpaged()).getContent());
        swapRequestRepo.deleteAll(swapRequestRepo.findByReceiverId(userId, Pageable.unpaged()).getContent());
        skillDetailsRepo.deleteAll(skillDetailsRepo.findByUserId(userId, Pageable.unpaged()).getContent());

        for (Long id : affectedRevieweeIds) {
            User reviewee = userRepo.findById(id).orElse(null);
            if (reviewee != null) {
                reviewee.setRating(reviewService.calculateAverageRating(id));
                userRepo.save(reviewee);
            }
        }

        userRepo.delete(user);
    }


}